package com.destrostudios.icetea.core.asset;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.io.IOException;
import java.io.InputStream;

@AllArgsConstructor
@Getter
public abstract class AssetKey {

    protected String key;

    public abstract InputStream openInputStream() throws IOException;
}
