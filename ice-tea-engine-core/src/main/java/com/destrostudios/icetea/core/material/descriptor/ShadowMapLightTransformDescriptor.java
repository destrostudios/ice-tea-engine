package com.destrostudios.icetea.core.material.descriptor;

import com.destrostudios.icetea.core.render.shadow.ShadowMapRenderJob;

public class ShadowMapLightTransformDescriptor extends UniformDescriptor<ShadowMapLightTransformDescriptorLayout> {

    public ShadowMapLightTransformDescriptor(String name, ShadowMapLightTransformDescriptorLayout layout, ShadowMapRenderJob shadowMapRenderJob) {
        super(name, layout, shadowMapRenderJob.getLightTransformUniformData());
    }
}
