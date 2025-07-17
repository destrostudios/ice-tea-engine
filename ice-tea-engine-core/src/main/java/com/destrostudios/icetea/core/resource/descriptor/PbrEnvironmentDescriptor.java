package com.destrostudios.icetea.core.resource.descriptor;

import com.destrostudios.icetea.core.clone.CloneContext;

import static org.lwjgl.vulkan.VK10.*;

public class PbrEnvironmentDescriptor extends UniformDescriptor {

    public PbrEnvironmentDescriptor() {
        super(VK_SHADER_STAGE_FRAGMENT_BIT);
    }

    public PbrEnvironmentDescriptor(PbrEnvironmentDescriptor pbrEnvironmentDescriptor, CloneContext context) {
        super(pbrEnvironmentDescriptor, context);
    }

    @Override
    public PbrEnvironmentDescriptor clone(CloneContext context) {
        return new PbrEnvironmentDescriptor(this, context);
    }
}
