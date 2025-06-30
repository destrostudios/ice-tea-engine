package com.destrostudios.icetea.core.asset.loader;

import com.destrostudios.icetea.core.asset.AssetLoader;

import java.io.IOException;
import java.io.InputStream;

public class StringLoader extends AssetLoader<String, Void> {

    @Override
    public String load() throws IOException {
        try (InputStream inputStream = assetKey.openInputStream()) {
            return new String(inputStream.readAllBytes());
        }
    }
}
