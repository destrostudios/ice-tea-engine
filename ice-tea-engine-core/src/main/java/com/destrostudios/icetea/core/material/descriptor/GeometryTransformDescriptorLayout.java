package com.destrostudios.icetea.core.material.descriptor;

import static org.lwjgl.vulkan.VK10.*;

public class GeometryTransformDescriptorLayout extends UniformDescriptorLayout {

    public GeometryTransformDescriptorLayout() {
        super(VK_SHADER_STAGE_VERTEX_BIT | VK_SHADER_STAGE_GEOMETRY_BIT | VK_SHADER_STAGE_FRAGMENT_BIT);
    }
}
