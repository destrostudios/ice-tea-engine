package com.destrostudios.icetea.core.shader;

import com.destrostudios.icetea.core.object.NativeObject;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VkPipelineShaderStageCreateInfo;
import org.lwjgl.vulkan.VkShaderModuleCreateInfo;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.LongBuffer;
import java.util.HashMap;
import java.util.LinkedHashMap;

import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.system.MemoryUtil.NULL;
import static org.lwjgl.util.shaderc.Shaderc.*;
import static org.lwjgl.vulkan.VK10.*;
import static org.lwjgl.vulkan.VK10.VK_SUCCESS;

public class ShaderManager extends NativeObject {

    private HashMap<String, String> filesCache = new HashMap<>();
    private LinkedHashMap<String, SPIRV> spirvCache = new LinkedHashMap<>();
    // TODO: Make configurable
    private int maximumCachedSPIRVs = 1000;

    public long createShaderModule(Shader shader, ShaderType shaderType, String additionalDeclarations) {
        try (MemoryStack stack = stackPush()) {
            ByteBuffer compiledShaderCode = getCompiledShaderCode(shader, shaderType, additionalDeclarations);

            VkShaderModuleCreateInfo shaderModuleCreateInfo = VkShaderModuleCreateInfo.callocStack(stack);
            shaderModuleCreateInfo.sType(VK_STRUCTURE_TYPE_SHADER_MODULE_CREATE_INFO);
            shaderModuleCreateInfo.pCode(compiledShaderCode);

            LongBuffer pShaderModule = stack.mallocLong(1);
            int result = vkCreateShaderModule(application.getLogicalDevice(), shaderModuleCreateInfo, null, pShaderModule);
            if (result != VK_SUCCESS) {
                throw new RuntimeException("Failed to create shader module (result = " + result + ")");
            }
            return pShaderModule.get(0);
        }
    }

    private ByteBuffer getCompiledShaderCode(Shader shader, ShaderType shaderType, String additionalDeclarations) {
        String combinedSource = getCombinedShaderSource(shader, additionalDeclarations);
        return getOrCompileShaderSource(shader.getFilePath(), combinedSource, shaderType);
    }

    private String getCombinedShaderSource(Shader shader, String additionalDeclarations) {
        String combinedSource = "";
        String[] shaderSourceLines = getShaderSource(shader.getFilePath()).split("\n");
        boolean addDeclarations = true;
        for (String shaderSourceLine : shaderSourceLines) {
            if (addDeclarations && (!shaderSourceLine.startsWith("#"))) {
                combinedSource += "\n";
                for (String shaderNode : shader.getRequiredShaderNodes()) {
                    combinedSource += getShaderSource(shaderNode) + "\n\n";
                }
                combinedSource += additionalDeclarations;
                addDeclarations = false;
            }
            combinedSource += shaderSourceLine + "\n";
        }
        return combinedSource;
    }

    private String getShaderSource(String shaderFile) {
        return filesCache.computeIfAbsent(shaderFile, sf -> {
            try {
                try (InputStream inputStream = application.getAssetManager().loadInputStream(shaderFile)) {
                    return new String(inputStream.readAllBytes());
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            return null;
        });
    }

    private ByteBuffer getOrCompileShaderSource(String filename, String source, ShaderType shaderType) {
        SPIRV spirv = spirvCache.get(source);
        if (spirv == null) {
            spirv = compile(filename, source, shaderType);
            spirvCache.put(source, spirv);
            ensureMaximumCacheSize();
        }
        return spirv.getByteCode();
    }

    private void ensureMaximumCacheSize() {
        if (spirvCache.size() > maximumCachedSPIRVs) {
            String oldestShaderSource = spirvCache.keySet().iterator().next();
            SPIRV removedSPIRV = spirvCache.remove(oldestShaderSource);
            removedSPIRV.free();
        }
    }

    private SPIRV compile(String filename, String source, ShaderType shaderType) {
        long compiler = shaderc_compiler_initialize();
        if (compiler == NULL) {
            throw new RuntimeException("Failed to create shader compiler");
        }
        long result = shaderc_compile_into_spv(compiler, source, shaderType.getKind(), filename, "main", NULL);
        if (result == NULL) {
            throw new RuntimeException("Failed to compile shader " + filename + " into SPIR-V (result = " + result + ")");
        }
        int status = shaderc_result_get_compilation_status(result);
        if (status != shaderc_compilation_status_success) {
            throw new RuntimeException("Failed to compile shader " + filename + "into SPIR-V (status = " + status + "):\n " + shaderc_result_get_error_message(result) + "\n" + source);
        }
        shaderc_compiler_release(compiler);
        return new SPIRV(result, shaderc_result_get_bytes(result));
    }

    public void createShaderStage(VkPipelineShaderStageCreateInfo.Buffer shaderStages, int shaderStageIndex, int shaderStage, long shaderModule, MemoryStack stack) {
        VkPipelineShaderStageCreateInfo shaderStageCreateInfo = shaderStages.get(shaderStageIndex);
        shaderStageCreateInfo.sType(VK_STRUCTURE_TYPE_PIPELINE_SHADER_STAGE_CREATE_INFO);
        shaderStageCreateInfo.stage(shaderStage);
        shaderStageCreateInfo.module(shaderModule);
        shaderStageCreateInfo.pName(stack.UTF8("main"));
    }

    @Override
    protected void cleanupNativeInternal() {
        for (SPIRV spirv : spirvCache.values()) {
            spirv.free();
        }
        spirvCache.clear();
        filesCache.clear();
        super.cleanupNativeInternal();
    }
}
