package com.destrostudios.icetea.core.materials.descriptors;

import static org.lwjgl.vulkan.VK10.VK_SHADER_STAGE_VERTEX_BIT;

public class CameraTransformDescriptorLayout extends UniformDescriptorLayout {

    public CameraTransformDescriptorLayout() {
        super(VK_SHADER_STAGE_VERTEX_BIT);
    }
}
