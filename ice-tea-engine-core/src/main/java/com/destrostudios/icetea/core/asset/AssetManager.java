package com.destrostudios.icetea.core.asset;

import com.destrostudios.icetea.core.asset.loader.*;
import com.destrostudios.icetea.core.font.BitmapFont;
import com.destrostudios.icetea.core.mesh.Mesh;
import com.destrostudios.icetea.core.scene.Spatial;
import com.destrostudios.icetea.core.texture.Texture;

import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;

// TODO: Map extensions to loaders and cache assets (combinable with cache in ShaderManager)
public class AssetManager {

    public AssetManager() {
        locators = new LinkedList<>();
    }
    private LinkedList<AssetLocator> locators;

    public void addLocator(AssetLocator locator) {
        locators.add(locator);
    }

    public void removeLocator(AssetLocator locator) {
        locators.remove(locator);
    }

    public Mesh loadMesh(String key) {
        return load(key, new ObjLoader(), null);
    }

    public Spatial loadModel(String key) {
        return loadModel(key, GltfLoaderSettings.builder().build());
    }

    public Spatial loadModel(String key, GltfLoaderSettings settings) {
        return load(key, new GltfLoader(), settings);
    }

    public Texture loadTexture(String key) {
        return load(key, new TextureLoader(), null);
    }

    public BitmapFont loadBitmapFont(String key) {
        return load(key, new BitmapFontLoader(), null);
    }

    private <T, S> T load(String key, AssetLoader<T, S> assetLoader, S settings) {
        assetLoader.setContext(this, key, settings);
        InputStream inputStream = load(key);
        try {
            return assetLoader.load(inputStream);
        } catch (IOException ex) {
            ex.printStackTrace();
            throw new RuntimeException("Error while loading asset");
        }
    }

    public InputStream load(String key) {
        for (AssetLocator locator : locators) {
            InputStream inputStream = locator.getInputStream(key);
            if (inputStream != null) {
                return inputStream;
            }
        }
        throw new AssetNotFoundException(key);
    }
}
