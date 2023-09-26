package com.destrostudios.icetea.core.asset.locator;

import com.destrostudios.icetea.core.asset.AssetKey;
import lombok.Getter;

import java.io.IOException;
import java.io.InputStream;

import static java.lang.ClassLoader.getSystemClassLoader;

@Getter
public class ClasspathAssetKey extends AssetKey {

    public ClasspathAssetKey(String key, String resourceName) {
        super(key);
        this.resourceName = resourceName;
    }
    private String resourceName;

    @Override
    public InputStream openInputStream() throws IOException {
        InputStream inputStream = getSystemClassLoader().getResourceAsStream(resourceName);
        if (inputStream != null) {
            return inputStream;
        }
        throw new IOException("Couldn't load classpath resource '" + resourceName + "'.");
    }
}
