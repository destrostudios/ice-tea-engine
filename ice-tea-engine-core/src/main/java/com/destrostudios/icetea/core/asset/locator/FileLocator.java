package com.destrostudios.icetea.core.asset.locator;

import com.destrostudios.icetea.core.asset.AssetLocator;
import lombok.AllArgsConstructor;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

@AllArgsConstructor
public class FileLocator implements AssetLocator {

    private String root;

    @Override
    public InputStream getInputStream(String key) {
        File file = new File(root, key);
        if (file.exists()) {
            try {
                return new FileInputStream(file);
            } catch (FileNotFoundException ex) {
                return null;
            }
        }
        return null;
    }
}
