package com.destrostudios.icetea.core.asset;

import com.destrostudios.icetea.core.asset.loader.GltfLoader;
import com.destrostudios.icetea.core.asset.loader.GltfLoaderSettings;
import com.destrostudios.icetea.core.asset.loader.ObjLoader;
import com.destrostudios.icetea.core.asset.loader.TextureLoader;
import com.destrostudios.icetea.core.mesh.Mesh;
import com.destrostudios.icetea.core.scene.Spatial;
import com.destrostudios.icetea.core.texture.Texture;

import java.io.IOException;
import java.io.InputStream;

import static java.lang.ClassLoader.getSystemClassLoader;

// TODO: Map extensions to loaders and cache assets
public class AssetManager {

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

    private <T, S> T load(String key, AssetLoader<T, S> assetLoader, S settings) {
        assetLoader.setContext(this, key, settings);
        InputStream inputStream = getInputStream(key);
        try {
            return assetLoader.load(inputStream);
        } catch (IOException ex) {
            ex.printStackTrace();
            throw new RuntimeException("Error while loading asset");
        }
    }

    private InputStream getInputStream(String key) {
        return getSystemClassLoader().getResourceAsStream(key);
    }
}
