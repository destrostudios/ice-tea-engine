package com.destrostudios.icetea.core.asset;

import java.io.InputStream;

public interface AssetLocator {

    InputStream getInputStream(String key);
}
