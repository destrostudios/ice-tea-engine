package com.destrostudios.icetea.core.asset;

import java.io.IOException;
import java.io.InputStream;
import java.util.function.Supplier;

public abstract class AssetLoader<T, S> {

    protected AssetManager assetManager;
    protected String key;
    protected S settings;

    public void setContext(AssetManager assetManager, String key, S settings) {
        this.assetManager = assetManager;
        this.key = key;
        this.settings = settings;
    }

    public abstract T load(Supplier<InputStream> inputStreamSupplier) throws IOException;
}
