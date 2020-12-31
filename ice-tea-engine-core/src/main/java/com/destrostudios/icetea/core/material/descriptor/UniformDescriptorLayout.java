package com.destrostudios.icetea.core.material.descriptor;

import org.lwjgl.vulkan.VkDescriptorSetLayoutBinding;

import static org.lwjgl.vulkan.VK10.VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER;

public class UniformDescriptorLayout extends MaterialDescriptorLayout {

    public UniformDescriptorLayout(int stageFlags) {
        this.stageFlags = stageFlags;
    }
    private int stageFlags;

    @Override
    public int getDescriptorType() {
        return VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER;
    }

    @Override
    public void initLayoutBinding(VkDescriptorSetLayoutBinding descriptorLayoutBinding) {
        descriptorLayoutBinding.stageFlags(stageFlags);
    }
}
