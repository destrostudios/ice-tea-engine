package com.destrostudios.icetea.core.resource.descriptor;

import com.destrostudios.icetea.core.clone.CloneContext;

import static org.lwjgl.vulkan.VK10.VK_SHADER_STAGE_VERTEX_BIT;

public class SkeletonDescriptor extends UniformDescriptor {

    public SkeletonDescriptor() {
        super(VK_SHADER_STAGE_VERTEX_BIT);
    }

    public SkeletonDescriptor(SkeletonDescriptor skeletonDescriptor, CloneContext context) {
        super(skeletonDescriptor, context);
    }

    @Override
    public SkeletonDescriptor clone(CloneContext context) {
        return new SkeletonDescriptor(this, context);
    }
}
