package com.destrostudios.icetea.core.resource.descriptor;

import com.destrostudios.icetea.core.clone.CloneContext;

import static org.lwjgl.vulkan.VK10.*;

public class SimpleTextureDescriptor extends TextureDescriptor {

    public SimpleTextureDescriptor() {
        super(VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER, VK_SHADER_STAGE_FRAGMENT_BIT | VK_SHADER_STAGE_GEOMETRY_BIT, false);
    }

    public SimpleTextureDescriptor(SimpleTextureDescriptor simpleTextureDescriptor, CloneContext context) {
        super(simpleTextureDescriptor, context);
    }

    @Override
    public SimpleTextureDescriptor clone(CloneContext context) {
        return new SimpleTextureDescriptor(this, context);
    }
}
