package com.destrostudios.icetea.core.shader;

import com.destrostudios.icetea.core.asset.AssetManager;
import com.destrostudios.icetea.core.object.NativeObject;
import com.destrostudios.icetea.core.shader.node.ShaderNodeManager;
import com.destrostudios.icetea.core.util.StringUtil;
import lombok.Getter;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VkPipelineShaderStageCreateInfo;
import org.lwjgl.vulkan.VkShaderModuleCreateInfo;

import java.nio.ByteBuffer;
import java.nio.LongBuffer;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.system.MemoryUtil.NULL;
import static org.lwjgl.util.shaderc.Shaderc.*;
import static org.lwjgl.vulkan.VK10.*;
import static org.lwjgl.vulkan.VK10.VK_SUCCESS;

public class ShaderManager extends NativeObject {

    public static final Pattern HOOK_PATTERN = Pattern.compile("// @hook (.+)");
    private static final Pattern IMPORT_PATTERN = Pattern.compile("// @import (.+)");

    public ShaderManager(AssetManager assetManager) {
        shaderNodeManager = new ShaderNodeManager(assetManager);
        // TODO: Register these only if samples are really used
        addLibraryRoot("core", "com/destrostudios/icetea/core/shaders/libs/");
        addLibraryRoot("samples", "com/destrostudios/icetea/samples/shaders/libs/");
        addNodeRoot("com/destrostudios/icetea/core/shaders/nodes/");
        addNodeRoot("com/destrostudios/icetea/samples/shaders/nodes/");
    }
    @Getter
    private ShaderNodeManager shaderNodeManager;
    private LinkedHashMap<String, SPIRV> spirvCache = new LinkedHashMap<>();
    // TODO: Make configurable
    private int maximumCachedSPIRVs = 1000;
    private HashMap<String, String> libraryRoots = new HashMap<>();

    public void addLibraryRoot(String prefix, String path) {
        libraryRoots.put(prefix, path);
    }

    public void removeLibraryRoot(String prefix) {
        libraryRoots.remove(prefix);
    }

    public void addNodeRoot(String path) {
        shaderNodeManager.addRootPath(path);
    }

    public void removeNodeRoot(String path) {
        shaderNodeManager.removeRootPath(path);
    }

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
        return getOrCompileShaderSource(combinedSource, shaderType, shader::getDebugIdentifier);
    }

    private String getCombinedShaderSource(Shader shader, String additionalDeclarations) {
        String combinedSource = "";
        String shaderSource = shader.getCode(application.getAssetManager());
        String[] shaderSourceLines = shaderSource.split("\n");
        boolean addDeclarations = true;
        for (String shaderSourceLine : shaderSourceLines) {
            Matcher importMatcher = IMPORT_PATTERN.matcher(shaderSourceLine);
            if (importMatcher.find()) {
                String libraryIdentifier = importMatcher.group(1);
                String libraryPath = getLibraryPath(libraryIdentifier);
                combinedSource += application.getAssetManager().loadString(libraryPath) + "\n\n";
            } else {
                if (addDeclarations && (!shaderSourceLine.startsWith("#"))) {
                    combinedSource += "\n" + additionalDeclarations;
                    addDeclarations = false;
                }
                combinedSource += shaderSourceLine + "\n";
            }
        }
        return combinedSource;
    }

    private String getLibraryPath(String libraryIdentifier) {
        for (Map.Entry<String, String> root : libraryRoots.entrySet()) {
            String prefix = root.getKey() + "/";
            if (libraryIdentifier.startsWith(prefix)) {
                return root.getValue() + libraryIdentifier.substring(prefix.length()) + ".glsllib";
            }
        }
        return libraryIdentifier;
    }

    private ByteBuffer getOrCompileShaderSource(String source, ShaderType shaderType, Supplier<String> getDebugIdentifier) {
        SPIRV spirv = spirvCache.get(source);
        if (spirv == null) {
            spirv = compile(source, shaderType, getDebugIdentifier.get());
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

    private SPIRV compile(String source, ShaderType shaderType, String debugIdentifier) {
        long compiler = shaderc_compiler_initialize();
        if (compiler == NULL) {
            throw new RuntimeException("Failed to create shader compiler");
        }
        long result = shaderc_compile_into_spv(compiler, source, shaderType.getKind(), debugIdentifier, "main", NULL);
        if (result == NULL) {
            throw new RuntimeException("Failed to compile shader " + debugIdentifier + " into SPIR-V (result = " + result + "):\n" + StringUtil.addLineNumbers(source));
        }
        int status = shaderc_result_get_compilation_status(result);
        if (status != shaderc_compilation_status_success) {
            throw new RuntimeException("Failed to compile shader " + debugIdentifier + " into SPIR-V (status = " + status + "):\n " + shaderc_result_get_error_message(result) + "\n" + StringUtil.addLineNumbers(source));
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
        shaderNodeManager.clear();
        for (SPIRV spirv : spirvCache.values()) {
            spirv.free();
        }
        spirvCache.clear();
        super.cleanupNativeInternal();
    }
}
