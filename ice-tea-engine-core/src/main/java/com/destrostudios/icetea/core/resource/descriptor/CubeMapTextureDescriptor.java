package com.destrostudios.icetea.core.resource.descriptor;

import com.destrostudios.icetea.core.clone.CloneContext;

import static org.lwjgl.vulkan.VK10.*;

public class CubeMapTextureDescriptor extends TextureDescriptor {

    public CubeMapTextureDescriptor() {
        super(VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER, VK_SHADER_STAGE_FRAGMENT_BIT, true, false);
    }

    public CubeMapTextureDescriptor(CubeMapTextureDescriptor simpleTextureDescriptor, CloneContext context) {
        super(simpleTextureDescriptor, context);
    }

    @Override
    public CubeMapTextureDescriptor clone(CloneContext context) {
        return new CubeMapTextureDescriptor(this, context);
    }
}
