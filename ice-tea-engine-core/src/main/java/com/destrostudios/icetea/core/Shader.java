package com.destrostudios.icetea.core;

public class Shader {

    public Shader(String filePath) {
        this(filePath, new String[0]);
    }

    public Shader(String filePath, String[] requiredShaderNodes) {
        this.filePath = filePath;
        this.requiredShaderNodes = requiredShaderNodes;
    }
    private String filePath;
    private String[] requiredShaderNodes;

    public SPIRV compile(ShaderType shaderType) {
        String combinedSource = "";
        String[] shaderSourceLines = ShaderSPIRVUtils.readSource(filePath).split(System.lineSeparator());
        boolean addNodeDeclarations = true;
        for (String shaderSourceLine : shaderSourceLines) {
            if (addNodeDeclarations && (!shaderSourceLine.startsWith("#"))) {
                for (String shaderNode : requiredShaderNodes) {
                    combinedSource += ShaderSPIRVUtils.readSource("shaders/nodes/" + shaderNode + ".glsllib") + System.lineSeparator();
                }
                addNodeDeclarations = false;
            }
            combinedSource += shaderSourceLine + System.lineSeparator();
        }
        return ShaderSPIRVUtils.compileShader(filePath, combinedSource, shaderType);
    }
}
