package com.destrostudios.icetea.core.materials.descriptors;

import static org.lwjgl.vulkan.VK10.VK_SHADER_STAGE_FRAGMENT_BIT;

public class MaterialParamsDescriptorLayout extends UniformDescriptorLayout {

    public MaterialParamsDescriptorLayout() {
        super(VK_SHADER_STAGE_FRAGMENT_BIT);
    }
}
