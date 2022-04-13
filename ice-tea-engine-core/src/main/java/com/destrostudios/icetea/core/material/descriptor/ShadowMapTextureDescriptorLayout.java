package com.destrostudios.icetea.core.material.descriptor;

import static org.lwjgl.vulkan.VK10.*;

public class ShadowMapTextureDescriptorLayout extends TextureDescriptorLayout {

    public ShadowMapTextureDescriptorLayout() {
        super(VK_SHADER_STAGE_FRAGMENT_BIT);
    }

    @Override
    public int getDescriptorType() {
        return VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER;
    }
}
