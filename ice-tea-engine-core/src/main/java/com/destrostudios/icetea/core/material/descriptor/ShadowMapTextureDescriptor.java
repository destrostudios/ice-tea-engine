package com.destrostudios.icetea.core.material.descriptor;

import com.destrostudios.icetea.core.render.shadow.ShadowMapRenderJob;

public class ShadowMapTextureDescriptor extends TextureDescriptor {

    public ShadowMapTextureDescriptor(String name, ShadowMapRenderJob shadowMapRenderJob) {
        super(name, shadowMapRenderJob.getShadowMapTexture());
    }
}
