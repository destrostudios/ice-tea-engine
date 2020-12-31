package com.destrostudios.icetea.core.material.descriptor;

import com.destrostudios.icetea.core.material.Material;

public class MaterialParamsDescriptor extends UniformDescriptor<MaterialParamsDescriptorLayout> {

    public MaterialParamsDescriptor(String name, MaterialParamsDescriptorLayout layout, Material material) {
        super(name, layout, material.getParameters());
    }
}
