package com.destrostudios.icetea.core.shader.node;

import com.destrostudios.icetea.core.asset.AssetManager;
import com.destrostudios.icetea.core.shader.Shader;
import lombok.EqualsAndHashCode;

import java.util.ArrayList;

@EqualsAndHashCode(callSuper = false, onlyExplicitlyIncluded = true)
public class NodeShader extends Shader {

    public NodeShader(ArrayList<String> nodeNames, String code) {
        this.nodeNames = nodeNames;
        this.code = code;
    }
    @EqualsAndHashCode. Include
    private ArrayList<String> nodeNames;
    private String code;

    @Override
    public String getCode(AssetManager assetManager) {
        return code;
    }

    @Override
    public String getDebugIdentifier() {
        String debugIdentifier = "NodeShader[";
        for (int i = 0; i < nodeNames.size(); i++) {
            if (i > 0) {
                debugIdentifier += ", ";
            }
            debugIdentifier += nodeNames.get(i);
        }
        debugIdentifier += "]";
        return debugIdentifier;
    }
}
