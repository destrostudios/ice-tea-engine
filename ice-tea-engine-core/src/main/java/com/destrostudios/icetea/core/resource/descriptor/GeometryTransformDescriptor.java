package com.destrostudios.icetea.core.resource.descriptor;

import com.destrostudios.icetea.core.clone.CloneContext;

import static org.lwjgl.vulkan.VK10.*;

public class GeometryTransformDescriptor extends UniformDescriptor {

    public GeometryTransformDescriptor() {
        super(VK_SHADER_STAGE_VERTEX_BIT | VK_SHADER_STAGE_TESSELLATION_CONTROL_BIT | VK_SHADER_STAGE_TESSELLATION_EVALUATION_BIT | VK_SHADER_STAGE_GEOMETRY_BIT);
    }

    public GeometryTransformDescriptor(GeometryTransformDescriptor geometryTransformDescriptor, CloneContext context) {
        super(geometryTransformDescriptor, context);
    }

    @Override
    public GeometryTransformDescriptor clone(CloneContext context) {
        return new GeometryTransformDescriptor(this, context);
    }
}
