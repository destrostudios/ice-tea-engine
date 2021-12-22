package com.destrostudios.icetea.core.asset.locator;

import com.destrostudios.icetea.core.asset.AssetLocator;

import java.io.InputStream;

import static java.lang.ClassLoader.getSystemClassLoader;

public class ClasspathLocator implements AssetLocator {

    @Override
    public InputStream getInputStream(String key) {
        return getSystemClassLoader().getResourceAsStream(key);
    }
}
