package com.destrostudios.icetea.core;

import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;

public class Material {

    public Material() {
        textures = new HashMap<>();
        parameters = new UniformData();
    }
    private Application application;
    @Getter
    @Setter
    private Shader vertexShader;
    @Getter
    @Setter
    private Shader fragmentShader;
    @Getter
    private UniformData parameters;
    @Getter
    private HashMap<String, Texture> textures;
    private int usingGeometriesCount;
    @Setter
    @Getter
    private boolean transparent;

    public boolean isInitialized() {
        return (application != null);
    }

    public void init(Application application) {
        this.application = application;
        for (Texture texture : textures.values()) {
            if (!texture.isInitialized()) {
                texture.init(application);
            }
        }
    }

    public void setTexture(String name, Texture texture) {
        textures.put(name, texture);
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
        for (Texture texture : textures.values()) {
            texture.cleanup();
        }
    }
}
