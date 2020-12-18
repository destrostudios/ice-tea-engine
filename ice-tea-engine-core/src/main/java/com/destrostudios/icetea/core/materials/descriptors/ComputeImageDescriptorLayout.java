package com.destrostudios.icetea.core.materials.descriptors;

import static org.lwjgl.vulkan.VK10.VK_DESCRIPTOR_TYPE_STORAGE_IMAGE;
import static org.lwjgl.vulkan.VK10.VK_SHADER_STAGE_COMPUTE_BIT;

public class ComputeImageDescriptorLayout extends TextureDescriptorLayout {

    public ComputeImageDescriptorLayout() {
        super(VK_SHADER_STAGE_COMPUTE_BIT);
    }

    @Override
    public int getDescriptorType() {
        return VK_DESCRIPTOR_TYPE_STORAGE_IMAGE;
    }
}
