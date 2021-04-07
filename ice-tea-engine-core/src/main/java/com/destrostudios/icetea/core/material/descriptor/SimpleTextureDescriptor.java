package com.destrostudios.icetea.core.material.descriptor;

import com.destrostudios.icetea.core.texture.Texture;

import static org.lwjgl.vulkan.VK10.VK_IMAGE_LAYOUT_SHADER_READ_ONLY_OPTIMAL;

public class SimpleTextureDescriptor extends TextureDescriptor {

    public SimpleTextureDescriptor(String name, Texture texture) {
        super(name, texture, VK_IMAGE_LAYOUT_SHADER_READ_ONLY_OPTIMAL);
    }
}
