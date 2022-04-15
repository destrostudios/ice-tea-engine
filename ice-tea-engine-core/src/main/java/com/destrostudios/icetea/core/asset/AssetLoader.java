package com.destrostudios.icetea.core.asset;

import java.io.IOException;
import java.io.InputStream;

public abstract class AssetLoader<T, S> {

    protected AssetManager assetManager;
    protected String key;
    protected S settings;

    public void setContext(AssetManager assetManager, String key, S settings) {
        this.assetManager = assetManager;
        this.key = key;
        this.settings = settings;
    }

    public abstract T load(InputStream inputStream) throws IOException;
}
