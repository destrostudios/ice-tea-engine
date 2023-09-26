package com.destrostudios.icetea.core.asset.locator;

import com.destrostudios.icetea.core.asset.AssetKey;
import lombok.Getter;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

@Getter
public class FileAssetKey extends AssetKey {

    public FileAssetKey(String key, Path path) {
        super(key);
        this.path = path;
    }
    private Path path;

    @Override
    public InputStream openInputStream() throws IOException {
        return Files.newInputStream(path);
    }
}
