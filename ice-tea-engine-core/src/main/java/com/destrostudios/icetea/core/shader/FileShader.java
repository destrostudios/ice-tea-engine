package com.destrostudios.icetea.core.shader;

import com.destrostudios.icetea.core.asset.AssetManager;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;

@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class FileShader extends Shader {

    private String path;

    @Override
    public String getCode(AssetManager assetManager) {
        return assetManager.loadString(path);
    }

    @Override
    public String getDebugIdentifier() {
        return "FileShader[" + path + "]";
    }
}
