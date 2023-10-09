package com.destrostudios.icetea.core.shader;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@AllArgsConstructor
@Getter
@EqualsAndHashCode
public class Shader {

    public Shader(String filePath) {
        this(filePath, new String[0]);
    }
    private String filePath;
    private String[] requiredShaderNodes;
}
