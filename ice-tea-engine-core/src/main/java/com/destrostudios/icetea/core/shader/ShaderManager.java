package com.destrostudios.icetea.core.shader;

import com.destrostudios.icetea.core.material.descriptor.MaterialDescriptorSet;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.LinkedHashMap;

import static java.lang.ClassLoader.getSystemClassLoader;
import static org.lwjgl.system.MemoryUtil.NULL;
import static org.lwjgl.util.shaderc.Shaderc.*;

public class ShaderManager {

    public ShaderManager() {
        filesCache = new HashMap<>();
        spirvCache = new LinkedHashMap<>();
        maximumCachedSPIRVs = 100;
    }
    private HashMap<String, String> filesCache;
    private LinkedHashMap<String, SPIRV> spirvCache;
    private int maximumCachedSPIRVs;

    public ByteBuffer getCompiledShaderCode(Shader shader, ShaderType shaderType, MaterialDescriptorSet materialDescriptorSet) {
        String combinedSource = getCombinedShaderSource(shader, materialDescriptorSet);
        return getOrCompileShaderSource(shader.getFilePath(), combinedSource, shaderType);
    }

    private String getCombinedShaderSource(Shader shader, MaterialDescriptorSet materialDescriptorSet) {
        String combinedSource = "";
        String[] shaderSourceLines = getShaderSource(shader.getFilePath()).split("\n");
        boolean addDeclarations = true;
        for (String shaderSourceLine : shaderSourceLines) {
            if (addDeclarations && (!shaderSourceLine.startsWith("#"))) {
                combinedSource += "\n";
                for (String shaderNode : shader.getRequiredShaderNodes()) {
                    combinedSource += getShaderSource("shaders/nodes/" + shaderNode + ".glsllib") + "\n\n";
                }
                combinedSource += materialDescriptorSet.getShaderDeclaration();
                addDeclarations = false;
            }
            combinedSource += shaderSourceLine + "\n";
        }
        return combinedSource;
    }

    private String getShaderSource(String shaderFile) {
        return filesCache.computeIfAbsent(shaderFile, sf -> {
            try {
                return new String(getSystemClassLoader().getResourceAsStream(shaderFile).readAllBytes());
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
            throw new RuntimeException("Failed to compile shader " + filename + " into SPIR-V");
        }
        if (shaderc_result_get_compilation_status(result) != shaderc_compilation_status_success) {
            throw new RuntimeException("Failed to compile shader " + filename + "into SPIR-V:\n " + shaderc_result_get_error_message(result) + "\n" + source);
        }
        shaderc_compiler_release(compiler);
        return new SPIRV(result, shaderc_result_get_bytes(result));
    }

    public void cleanup() {
        for (SPIRV spirv : spirvCache.values()) {
            spirv.free();
        }
        spirvCache.clear();
    }
}
