package com.destrostudios.icetea.core.material.descriptor;

import com.destrostudios.icetea.core.render.shadow.ShadowMapRenderJob;

public class ShadowMapLightTransformDescriptor extends UniformDescriptor {

    public ShadowMapLightTransformDescriptor(String name, ShadowMapRenderJob shadowMapRenderJob) {
        super(name, shadowMapRenderJob.getLightTransformUniformData());
    }
}
