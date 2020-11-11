package com.destrostudios.icetea.core;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class Material {

    public Material() {
        textures = new ArrayList<>();
    }
    private String vertexShaderFile;
    private String fragmentShaderFile;
    private List<Texture> textures;
    private int usingGeometriesCount;

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
        for (Texture texture : textures) {
            texture.cleanup();
        }
    }
}
