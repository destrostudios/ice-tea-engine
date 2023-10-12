package com.destrostudios.icetea.core.resource.descriptor;

import com.destrostudios.icetea.core.clone.CloneContext;

import static org.lwjgl.vulkan.VK10.VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER;
import static org.lwjgl.vulkan.VK10.VK_SHADER_STAGE_COMPUTE_BIT;

public class NormalMapDescriptor extends TextureDescriptor {

    public NormalMapDescriptor() {
        super(VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER, VK_SHADER_STAGE_COMPUTE_BIT, false);
    }

    public NormalMapDescriptor(NormalMapDescriptor normalMapDescriptor, CloneContext context) {
        super(normalMapDescriptor, context);
    }

    @Override
    public NormalMapDescriptor clone(CloneContext context) {
        return new NormalMapDescriptor(this, context);
    }
}
