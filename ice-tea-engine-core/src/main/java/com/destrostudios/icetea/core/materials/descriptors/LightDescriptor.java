package com.destrostudios.icetea.core.materials.descriptors;

import com.destrostudios.icetea.core.Light;

public class LightDescriptor extends UniformDescriptor<LightDescriptorLayout> {

    public LightDescriptor(String name, LightDescriptorLayout layout, Light light) {
        super(name, layout, light.getUniformData());
    }
}
