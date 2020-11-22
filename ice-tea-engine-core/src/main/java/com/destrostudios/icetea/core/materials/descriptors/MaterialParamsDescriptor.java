package com.destrostudios.icetea.core.materials.descriptors;

import com.destrostudios.icetea.core.Material;

public class MaterialParamsDescriptor extends UniformDescriptor<MaterialParamsDescriptorLayout> {

    public MaterialParamsDescriptor(String name, MaterialParamsDescriptorLayout layout, Material material) {
        super(name, layout, material.getParameters());
    }
}
