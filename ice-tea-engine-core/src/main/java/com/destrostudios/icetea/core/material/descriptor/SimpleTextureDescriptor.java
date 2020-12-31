package com.destrostudios.icetea.core.material.descriptor;

import com.destrostudios.icetea.core.Texture;

import static org.lwjgl.vulkan.VK10.VK_IMAGE_LAYOUT_SHADER_READ_ONLY_OPTIMAL;

public class SimpleTextureDescriptor extends TextureDescriptor<SimpleTextureDescriptorLayout> {

    public SimpleTextureDescriptor(String name, SimpleTextureDescriptorLayout layout, Texture texture) {
        super(name, layout, texture, VK_IMAGE_LAYOUT_SHADER_READ_ONLY_OPTIMAL);
    }
}
