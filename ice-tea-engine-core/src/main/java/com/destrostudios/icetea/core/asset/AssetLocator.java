package com.destrostudios.icetea.core.asset;

public interface AssetLocator<K extends AssetKey> {

    K findAsset(String key);
}
