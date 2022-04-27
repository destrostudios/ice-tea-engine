package com.destrostudios.icetea.core.resource.descriptor;

import com.destrostudios.icetea.core.clone.CloneContext;

import static org.lwjgl.vulkan.VK10.VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER;
import static org.lwjgl.vulkan.VK10.VK_SHADER_STAGE_FRAGMENT_BIT;

public class ShadowMapTextureDescriptor extends TextureDescriptor {

    public ShadowMapTextureDescriptor() {
        super(VK_SHADER_STAGE_FRAGMENT_BIT);
    }

    public ShadowMapTextureDescriptor(ShadowMapTextureDescriptor shadowMapTextureDescriptor, CloneContext context) {
        super(shadowMapTextureDescriptor, context);
    }

    @Override
    protected int getDescriptorType() {
        return VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER;
    }

    @Override
    public ShadowMapTextureDescriptor clone(CloneContext context) {
        return new ShadowMapTextureDescriptor(this, context);
    }
}
