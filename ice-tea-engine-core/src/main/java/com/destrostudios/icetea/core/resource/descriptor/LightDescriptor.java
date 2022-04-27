package com.destrostudios.icetea.core.resource.descriptor;

import com.destrostudios.icetea.core.clone.CloneContext;

import static org.lwjgl.vulkan.VK10.*;

public class LightDescriptor extends UniformDescriptor {

    public LightDescriptor() {
        super(VK_SHADER_STAGE_VERTEX_BIT | VK_SHADER_STAGE_GEOMETRY_BIT | VK_SHADER_STAGE_FRAGMENT_BIT);
    }

    public LightDescriptor(LightDescriptor lightDescriptor, CloneContext context) {
        super(lightDescriptor, context);
    }

    @Override
    public LightDescriptor clone(CloneContext context) {
        return new LightDescriptor(this, context);
    }
}
