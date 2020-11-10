package com.destrostudios.icetea.core;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Material {

    private String vertexShaderFile;
    private String fragmentShaderFile;
    private Texture texture;

    public void cleanup() {
        texture.cleanup();
    }
}
