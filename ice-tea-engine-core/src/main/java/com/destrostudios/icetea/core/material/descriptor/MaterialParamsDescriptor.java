package com.destrostudios.icetea.core.material.descriptor;

import com.destrostudios.icetea.core.material.Material;

public class MaterialParamsDescriptor extends UniformDescriptor {

    public MaterialParamsDescriptor(String name, Material material) {
        super(name, material.getParameters());
    }
}
