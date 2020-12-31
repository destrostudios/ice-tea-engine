package com.destrostudios.icetea.core.material.descriptor;

import static org.lwjgl.vulkan.VK10.*;

public class NormalMapDescriptorLayout extends TextureDescriptorLayout {

    public NormalMapDescriptorLayout() {
        super(VK_SHADER_STAGE_COMPUTE_BIT);
    }

    @Override
    public int getDescriptorType() {
        return VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER;
    }
}
