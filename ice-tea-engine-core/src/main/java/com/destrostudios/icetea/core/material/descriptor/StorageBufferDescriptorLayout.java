package com.destrostudios.icetea.core.material.descriptor;

import org.lwjgl.vulkan.VkDescriptorSetLayoutBinding;

import static org.lwjgl.vulkan.VK10.VK_DESCRIPTOR_TYPE_STORAGE_BUFFER;

public class StorageBufferDescriptorLayout extends MaterialDescriptorLayout {

    public StorageBufferDescriptorLayout(int stageFlags) {
        this.stageFlags = stageFlags;
    }
    private int stageFlags;

    @Override
    public int getDescriptorType() {
        return VK_DESCRIPTOR_TYPE_STORAGE_BUFFER;
    }

    @Override
    public void initLayoutBinding(VkDescriptorSetLayoutBinding descriptorLayoutBinding) {
        descriptorLayoutBinding.stageFlags(stageFlags);
    }
}
