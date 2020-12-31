package com.destrostudios.icetea.core.shader;

import com.destrostudios.icetea.core.material.descriptor.MaterialDescriptorSet;

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

    public SPIRV compile(ShaderType shaderType, MaterialDescriptorSet materialDescriptorSet) {
        String combinedSource = "";
        String[] shaderSourceLines = ShaderSPIRVUtils.readSource(filePath).split("\n");
        boolean addDeclarations = true;
        for (String shaderSourceLine : shaderSourceLines) {
            if (addDeclarations && (!shaderSourceLine.startsWith("#"))) {
                combinedSource += "\n";
                for (String shaderNode : requiredShaderNodes) {
                    combinedSource += ShaderSPIRVUtils.readSource("shaders/nodes/" + shaderNode + ".glsllib") + "\n\n";
                }
                combinedSource += materialDescriptorSet.getShaderDeclaration();
                addDeclarations = false;
            }
            combinedSource += shaderSourceLine + "\n";
        }
        return ShaderSPIRVUtils.compileShader(filePath, combinedSource, shaderType);
    }
}
