package com.destrostudios.icetea.core.asset.locator;

import com.destrostudios.icetea.core.asset.AssetLocator;

import static java.lang.ClassLoader.getSystemClassLoader;

public class ClasspathLocator implements AssetLocator<ClasspathAssetKey> {

    @Override
    public ClasspathAssetKey findAsset(String key) {
        if (getSystemClassLoader().getResource(key) != null) {
            return new ClasspathAssetKey(key, key);
        }
        return null;
    }
}
