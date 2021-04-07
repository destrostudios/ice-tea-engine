package com.destrostudios.icetea.core.material.descriptor;

import com.destrostudios.icetea.core.texture.Texture;

import static org.lwjgl.vulkan.VK10.VK_IMAGE_LAYOUT_GENERAL;

public class NormalMapDescriptor extends TextureDescriptor {

    public NormalMapDescriptor(String name, Texture texture) {
        super(name, texture, VK_IMAGE_LAYOUT_GENERAL);
    }
}
