package com.destrostudios.icetea.core.asset;

import java.io.IOException;

public abstract class AssetLoader<T, S> {

    protected AssetManager assetManager;
    protected AssetKey assetKey;
    protected S settings;

    public void setContext(AssetManager assetManager, AssetKey assetKey, S settings) {
        this.assetManager = assetManager;
        this.assetKey = assetKey;
        this.settings = settings;
    }

    public abstract T load() throws IOException;
}
