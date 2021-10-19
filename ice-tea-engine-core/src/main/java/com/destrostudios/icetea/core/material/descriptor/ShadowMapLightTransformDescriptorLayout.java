package com.destrostudios.icetea.core.material.descriptor;

import static org.lwjgl.vulkan.VK10.VK_SHADER_STAGE_GEOMETRY_BIT;
import static org.lwjgl.vulkan.VK10.VK_SHADER_STAGE_VERTEX_BIT;

public class ShadowMapLightTransformDescriptorLayout extends UniformDescriptorLayout {

    public ShadowMapLightTransformDescriptorLayout() {
        super(VK_SHADER_STAGE_VERTEX_BIT | VK_SHADER_STAGE_GEOMETRY_BIT);
    }
}
