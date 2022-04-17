package com.destrostudios.icetea.core.asset;

import java.io.InputStream;
import java.util.function.Supplier;

public interface AssetLocator {

    Supplier<InputStream> getInputStream(String key);
}
