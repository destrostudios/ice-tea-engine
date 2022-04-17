package com.destrostudios.icetea.core.asset.locator;

import com.destrostudios.icetea.core.asset.AssetLocator;

import java.io.InputStream;
import java.util.function.Supplier;

import static java.lang.ClassLoader.getSystemClassLoader;

public class ClasspathLocator implements AssetLocator {

    @Override
    public Supplier<InputStream> getInputStream(String key) {
        if (getSystemClassLoader().getResource(key) != null) {
            return () -> getSystemClassLoader().getResourceAsStream(key);
        }
        return null;
    }
}
