package com.destrostudios.icetea.core.asset.locator;

import com.destrostudios.icetea.core.asset.AssetLocator;
import lombok.AllArgsConstructor;

import java.nio.file.Files;
import java.nio.file.Path;

@AllArgsConstructor
public class FileLocator implements AssetLocator<FileAssetKey> {

    private String root;

    @Override
    public FileAssetKey findAsset(String key) {
        Path path = Path.of(root, key);
        if (Files.exists(path)) {
            return new FileAssetKey(key, path);
        }
        return null;
    }
}
