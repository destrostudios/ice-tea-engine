package com.destrostudios.icetea.core.resource.descriptor;

import com.destrostudios.icetea.core.clone.CloneContext;

import static org.lwjgl.vulkan.VK10.*;

public class MaterialParamsDescriptor extends UniformDescriptor {

    public MaterialParamsDescriptor() {
        super(VK_SHADER_STAGE_TESSELLATION_CONTROL_BIT | VK_SHADER_STAGE_TESSELLATION_EVALUATION_BIT | VK_SHADER_STAGE_GEOMETRY_BIT | VK_SHADER_STAGE_VERTEX_BIT | VK_SHADER_STAGE_FRAGMENT_BIT);
    }

    public MaterialParamsDescriptor(MaterialParamsDescriptor materialParamsDescriptor, CloneContext context) {
        super(materialParamsDescriptor, context);
    }

    @Override
    public MaterialParamsDescriptor clone(CloneContext context) {
        return new MaterialParamsDescriptor(this, context);
    }
}
