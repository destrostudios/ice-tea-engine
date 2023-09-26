package com.destrostudios.icetea.core.asset;

import com.destrostudios.icetea.core.asset.loader.*;
import com.destrostudios.icetea.core.font.BitmapFont;
import com.destrostudios.icetea.core.mesh.Mesh;
import com.destrostudios.icetea.core.scene.Spatial;
import com.destrostudios.icetea.core.texture.BufferedTexture;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.LinkedList;

// TODO: Map extensions to loaders and combine cache with the one in ShaderManager
// TODO: Settings should be in cache key, so that you don't get a wrongly configured asset
public class AssetManager {

    public AssetManager() {
        locators = new LinkedList<>();
        cachedMeshes = new HashMap<>();
        cachedModels = new HashMap<>();
        cachedTextures = new HashMap<>();
        cachedBitmapFonts = new HashMap<>();
    }
    private HashMap<String, Mesh> cachedMeshes;
    private HashMap<String, Spatial> cachedModels;
    private HashMap<String, BufferedTexture> cachedTextures;
    private HashMap<String, BitmapFont> cachedBitmapFonts;
    private LinkedList<AssetLocator> locators;

    public void addLocator(AssetLocator locator) {
        locators.add(locator);
    }

    public void removeLocator(AssetLocator locator) {
        locators.remove(locator);
    }

    public Mesh loadMesh(String key) {
        return cachedMeshes.computeIfAbsent(key, k -> load(k, new ObjLoader(), null));
    }

    public Spatial loadModel(String key) {
        return loadModel(key, GltfLoaderSettings.builder().build());
    }

    public Spatial loadModel(String key, GltfLoaderSettings settings) {
        return cachedModels.computeIfAbsent(key, k -> load(k, new GltfLoader(), settings)).clone(settings.getCloneContext());
    }

    public BufferedTexture loadTexture(String key) {
        return loadTexture(key, BufferedTextureLoaderSettings.builder().build());
    }

    public BufferedTexture loadTexture(String key, BufferedTextureLoaderSettings settings) {
        return cachedTextures.computeIfAbsent(key, k -> load(k, new BufferedTextureLoader(), settings));
    }

    public BitmapFont loadBitmapFont(String key) {
        return cachedBitmapFonts.computeIfAbsent(key, k -> load(k, new BitmapFontLoader(), null));
    }

    private <T, S> T load(String key, AssetLoader<T, S> assetLoader, S settings) {
        AssetKey assetKey = findAsset(key);
        assetLoader.setContext(this, assetKey, settings);
        try {
            return assetLoader.load();
        } catch (IOException ex) {
            throw new RuntimeException("Error while loading asset", ex);
        }
    }

    public InputStream loadInputStream(String key) {
        AssetKey assetKey = findAsset(key);
        try {
            return assetKey.openInputStream();
        } catch (IOException ex) {
            throw new RuntimeException("Error while loading asset", ex);
        }
    }

    private AssetKey findAsset(String key) {
        for (AssetLocator<?> locator : locators) {
            AssetKey assetKey = locator.findAsset(key);
            if (assetKey != null) {
                return assetKey;
            }
        }
        throw new AssetNotFoundException(key);
    }

    public void cleanup() {
        cleanupNative();
        cachedMeshes.clear();
        cachedModels.clear();
        cachedTextures.clear();
    }

    public void cleanupNative() {
        for (Mesh mesh : cachedMeshes.values()) {
            mesh.cleanupNative();
        }
        for (Spatial spatial : cachedModels.values()) {
            spatial.cleanupNativeState();
        }
        for (BufferedTexture texture : cachedTextures.values()) {
            texture.cleanupNative();
        }
    }
}
