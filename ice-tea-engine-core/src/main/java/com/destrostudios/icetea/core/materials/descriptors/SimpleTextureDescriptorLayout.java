package com.destrostudios.icetea.core.materials.descriptors;

import static org.lwjgl.vulkan.VK10.VK_SHADER_STAGE_FRAGMENT_BIT;

public class SimpleTextureDescriptorLayout extends TextureDescriptorLayout {

    public SimpleTextureDescriptorLayout() {
        super(VK_SHADER_STAGE_FRAGMENT_BIT);
    }
}
