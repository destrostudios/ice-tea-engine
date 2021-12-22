package com.destrostudios.icetea.core.asset;

public class AssetNotFoundException extends RuntimeException {

    public AssetNotFoundException(String key) {
        super("Asset with key \"" + key + "\" not found.");
    }
}
