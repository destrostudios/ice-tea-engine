package com.destrostudios.icetea.core.materials.descriptors;

import static org.lwjgl.vulkan.VK10.*;

public class CameraTransformDescriptorLayout extends UniformDescriptorLayout {

    public CameraTransformDescriptorLayout() {
        super(VK_SHADER_STAGE_VERTEX_BIT | VK_SHADER_STAGE_TESSELLATION_CONTROL_BIT | VK_SHADER_STAGE_TESSELLATION_EVALUATION_BIT | VK_SHADER_STAGE_GEOMETRY_BIT | VK_SHADER_STAGE_FRAGMENT_BIT);
    }
}
