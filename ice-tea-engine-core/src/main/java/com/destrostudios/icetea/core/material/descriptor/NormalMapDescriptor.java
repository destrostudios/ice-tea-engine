package com.destrostudios.icetea.core.material.descriptor;

import com.destrostudios.icetea.core.texture.Texture;

import static org.lwjgl.vulkan.VK10.VK_IMAGE_LAYOUT_GENERAL;

public class NormalMapDescriptor extends TextureDescriptor<NormalMapDescriptorLayout> {

    public NormalMapDescriptor(String name, NormalMapDescriptorLayout layout, Texture texture) {
        super(name, layout, texture, VK_IMAGE_LAYOUT_GENERAL);
    }
}
