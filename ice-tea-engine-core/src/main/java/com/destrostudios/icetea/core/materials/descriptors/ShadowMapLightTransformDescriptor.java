package com.destrostudios.icetea.core.materials.descriptors;

import com.destrostudios.icetea.core.ShadowMapRenderJob;

public class ShadowMapLightTransformDescriptor extends UniformDescriptor<ShadowMapLightTransformDescriptorLayout> {

    public ShadowMapLightTransformDescriptor(String name, ShadowMapLightTransformDescriptorLayout layout, ShadowMapRenderJob shadowMapRenderJob) {
        super(name, layout, shadowMapRenderJob.getLightTransformUniformData());
    }
}
