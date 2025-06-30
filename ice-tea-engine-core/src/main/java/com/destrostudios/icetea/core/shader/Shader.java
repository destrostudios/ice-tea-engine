package com.destrostudios.icetea.core.shader;

import com.destrostudios.icetea.core.asset.AssetManager;

public abstract class Shader {

    public abstract String getCode(AssetManager assetManager);

    public abstract String getDebugIdentifier();
}
