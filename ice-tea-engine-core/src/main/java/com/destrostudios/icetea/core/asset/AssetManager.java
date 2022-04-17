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
import java.util.function.Supplier;

// TODO: Map extensions to loaders and maybe combine cache with the one in ShaderManager
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
        return cachedTextures.computeIfAbsent(key, k -> load(k, new BufferedTextureLoader(), null));
    }

    public BitmapFont loadBitmapFont(String key) {
        return cachedBitmapFonts.computeIfAbsent(key, k -> load(k, new BitmapFontLoader(), null));
    }

    private <T, S> T load(String key, AssetLoader<T, S> assetLoader, S settings) {
        assetLoader.setContext(this, key, settings);
        Supplier<InputStream> inputStreamSupplier = load(key);
        try {
            return assetLoader.load(inputStreamSupplier);
        } catch (IOException ex) {
            throw new RuntimeException("Error while loading asset", ex);
        }
    }

    public Supplier<InputStream> load(String key) {
        for (AssetLocator locator : locators) {
            Supplier<InputStream> inputStreamSupplier = locator.getInputStream(key);
            if (inputStreamSupplier != null) {
                return inputStreamSupplier;
            }
        }
        throw new AssetNotFoundException(key);
    }

    public void cleanup() {
        for (Spatial spatial : cachedModels.values()) {
            spatial.cleanup();
        }
    }
}
