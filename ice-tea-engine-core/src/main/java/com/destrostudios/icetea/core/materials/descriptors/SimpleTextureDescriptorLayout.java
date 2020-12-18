package com.destrostudios.icetea.core.materials.descriptors;

import static org.lwjgl.vulkan.VK10.*;

public class SimpleTextureDescriptorLayout extends TextureDescriptorLayout {

    public SimpleTextureDescriptorLayout() {
        super(VK_SHADER_STAGE_FRAGMENT_BIT | VK_SHADER_STAGE_GEOMETRY_BIT);
    }

    @Override
    public int getDescriptorType() {
        return VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER;
    }
}
