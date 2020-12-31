package com.destrostudios.icetea.core.material.descriptor;

import com.destrostudios.icetea.core.light.Light;

public class LightDescriptor extends UniformDescriptor<LightDescriptorLayout> {

    public LightDescriptor(String name, LightDescriptorLayout layout, Light light) {
        super(name, layout, light.getUniformData());
    }
}
