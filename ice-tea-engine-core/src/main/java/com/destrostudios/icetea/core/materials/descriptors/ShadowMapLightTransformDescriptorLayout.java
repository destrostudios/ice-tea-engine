package com.destrostudios.icetea.core.materials.descriptors;

import static org.lwjgl.vulkan.VK10.VK_SHADER_STAGE_VERTEX_BIT;

public class ShadowMapLightTransformDescriptorLayout extends UniformDescriptorLayout {

    public ShadowMapLightTransformDescriptorLayout() {
        super(VK_SHADER_STAGE_VERTEX_BIT);
    }
}
