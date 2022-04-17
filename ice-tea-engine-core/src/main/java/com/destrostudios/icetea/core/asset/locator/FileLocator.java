package com.destrostudios.icetea.core.asset.locator;

import com.destrostudios.icetea.core.asset.AssetLocator;
import lombok.AllArgsConstructor;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.function.Supplier;

@AllArgsConstructor
public class FileLocator implements AssetLocator {

    private String root;

    @Override
    public Supplier<InputStream> getInputStream(String key) {
        File file = new File(root, key);
        if (file.exists()) {
            return () -> {
                try {
                    return new FileInputStream(file);
                } catch (FileNotFoundException ex) {
                    throw new RuntimeException("File no longer exists: " + key);
                }
            };
        }
        return null;
    }
}
