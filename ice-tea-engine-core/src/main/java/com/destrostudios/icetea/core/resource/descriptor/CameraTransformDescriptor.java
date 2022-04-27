package com.destrostudios.icetea.core.resource.descriptor;

import com.destrostudios.icetea.core.clone.CloneContext;

import static org.lwjgl.vulkan.VK10.*;
import static org.lwjgl.vulkan.VK10.VK_SHADER_STAGE_FRAGMENT_BIT;

public class CameraTransformDescriptor extends UniformDescriptor {

    public CameraTransformDescriptor() {
        super(VK_SHADER_STAGE_VERTEX_BIT | VK_SHADER_STAGE_TESSELLATION_CONTROL_BIT | VK_SHADER_STAGE_GEOMETRY_BIT | VK_SHADER_STAGE_FRAGMENT_BIT);
    }

    public CameraTransformDescriptor(CameraTransformDescriptor cameraTransformDescriptor, CloneContext context) {
        super(cameraTransformDescriptor, context);
    }

    @Override
    public CameraTransformDescriptor clone(CloneContext context) {
        return new CameraTransformDescriptor(this, context);
    }
}
