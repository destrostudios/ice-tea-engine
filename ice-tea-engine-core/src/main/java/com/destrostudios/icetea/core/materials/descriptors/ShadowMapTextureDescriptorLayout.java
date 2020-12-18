package com.destrostudios.icetea.core.materials.descriptors;

import static org.lwjgl.vulkan.VK10.VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER;
import static org.lwjgl.vulkan.VK10.VK_SHADER_STAGE_FRAGMENT_BIT;

public class ShadowMapTextureDescriptorLayout extends TextureDescriptorLayout {

    public ShadowMapTextureDescriptorLayout() {
        super(VK_SHADER_STAGE_FRAGMENT_BIT);
    }

    @Override
    public int getDescriptorType() {
        return VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER;
    }
}
