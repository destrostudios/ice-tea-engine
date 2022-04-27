package com.destrostudios.icetea.core.resource.descriptor;

import com.destrostudios.icetea.core.clone.CloneContext;

import static org.lwjgl.vulkan.VK10.*;

public class ShadowMapLightTransformDescriptor extends UniformDescriptor {

    public ShadowMapLightTransformDescriptor() {
        super(VK_SHADER_STAGE_VERTEX_BIT | VK_SHADER_STAGE_TESSELLATION_CONTROL_BIT | VK_SHADER_STAGE_GEOMETRY_BIT);
    }

    public ShadowMapLightTransformDescriptor(ShadowMapLightTransformDescriptor shadowMapLightTransformDescriptor, CloneContext context) {
        super(shadowMapLightTransformDescriptor, context);
    }

    @Override
    public ShadowMapLightTransformDescriptor clone(CloneContext context) {
        return new ShadowMapLightTransformDescriptor(this, context);
    }
}
