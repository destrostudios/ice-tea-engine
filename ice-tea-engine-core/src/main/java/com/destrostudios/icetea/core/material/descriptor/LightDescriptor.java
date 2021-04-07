package com.destrostudios.icetea.core.material.descriptor;

import com.destrostudios.icetea.core.light.Light;

public class LightDescriptor extends UniformDescriptor {

    public LightDescriptor(String name, Light light) {
        super(name, light.getUniformData());
    }
}
