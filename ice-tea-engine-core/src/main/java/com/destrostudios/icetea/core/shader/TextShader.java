package com.destrostudios.icetea.core.shader;

import com.destrostudios.icetea.core.asset.AssetManager;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;

@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class TextShader extends Shader {

    private String code;

    @Override
    public String getCode(AssetManager assetManager) {
        return code;
    }

    @Override
    public String getDebugIdentifier() {
        return "TextShader[length = " + code.length() + ", lines = " + code.split("\n").length + "]";
    }
}
