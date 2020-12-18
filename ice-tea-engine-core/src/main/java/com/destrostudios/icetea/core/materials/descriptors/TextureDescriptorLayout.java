package com.destrostudios.icetea.core.materials.descriptors;

import com.destrostudios.icetea.core.MaterialDescriptorLayout;
import org.lwjgl.vulkan.VkDescriptorSetLayoutBinding;

public abstract class TextureDescriptorLayout extends MaterialDescriptorLayout {

    public TextureDescriptorLayout(int stageFlags) {
        this.stageFlags = stageFlags;
    }
    private int stageFlags;

    @Override
    public void initLayoutBinding(VkDescriptorSetLayoutBinding descriptorLayoutBinding) {
        descriptorLayoutBinding.stageFlags(stageFlags);
    }
}
