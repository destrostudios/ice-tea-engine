package com.destrostudios.icetea.core.asset;

import java.io.IOException;
import java.io.InputStream;

public abstract class AssetLoader<T> {

    protected AssetManager assetManager;
    protected String key;

    public void setContext(AssetManager assetManager, String key) {
        this.assetManager = assetManager;
        this.key = key;
    }

    public abstract T load(InputStream inputStream) throws IOException;
}
