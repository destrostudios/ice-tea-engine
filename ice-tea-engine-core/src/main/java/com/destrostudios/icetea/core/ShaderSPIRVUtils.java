package com.destrostudios.icetea.core;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.lwjgl.system.MemoryUtil.NULL;
import static org.lwjgl.util.shaderc.Shaderc.*;

public class ShaderSPIRVUtils {

    public static String readSource(String shaderFile) {
        try {
            String externalFormFilePath = ClassLoader.getSystemClassLoader().getResource(shaderFile).toExternalForm();
            return new String(Files.readAllBytes(Paths.get(new URI(externalFormFilePath))));
        } catch (IOException | URISyntaxException ex) {
            ex.printStackTrace();
        }
        return null;
    }

    public static SPIRV compileShader(String filename, String source, ShaderType shaderType) {
        long compiler = shaderc_compiler_initialize();
        if (compiler == NULL) {
            throw new RuntimeException("Failed to create shader compiler");
        }
        long result = shaderc_compile_into_spv(compiler, source, shaderType.getKind(), filename, "main", NULL);
        if (result == NULL) {
            throw new RuntimeException("Failed to compile shader " + filename + " into SPIR-V");
        }
        if (shaderc_result_get_compilation_status(result) != shaderc_compilation_status_success) {
            throw new RuntimeException("Failed to compile shader " + filename + "into SPIR-V:\n " + shaderc_result_get_error_message(result));
        }
        shaderc_compiler_release(compiler);
        return new SPIRV(result, shaderc_result_get_bytes(result));
    }
}
