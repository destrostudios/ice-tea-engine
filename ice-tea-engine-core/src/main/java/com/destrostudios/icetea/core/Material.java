package com.destrostudios.icetea.core;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

public class Material {

    public Material() {
        textures = new ArrayList<>();
        parameters = new UniformData();
    }
    @Getter
    @Setter
    private Shader vertexShader;
    @Getter
    @Setter
    private Shader fragmentShader;
    @Getter
    private UniformData parameters;
    @Getter
    private List<Texture> textures;
    private int usingGeometriesCount;
    @Setter
    @Getter
    private boolean transparent;

    public void addTexture(Texture texture) {
        textures.add(texture);
    }

    public void increaseUsingGeometriesCount() {
        usingGeometriesCount++;
    }

    public void decreaseUsingGeometriesCount() {
        usingGeometriesCount--;
    }

    public boolean isUnused() {
        return (usingGeometriesCount <= 0);
    }

    public void cleanup() {
        parameters.cleanupBuffer();
        for (Texture texture : textures) {
            texture.cleanup();
        }
    }
}
