package com.destrostudios.icetea.core;

import org.lwjgl.vulkan.VkDescriptorSetLayoutBinding;

public abstract class MaterialDescriptorLayout {

    public abstract int getDescriptorType();

    public abstract void initLayoutBinding(VkDescriptorSetLayoutBinding descriptorLayoutBinding);
}