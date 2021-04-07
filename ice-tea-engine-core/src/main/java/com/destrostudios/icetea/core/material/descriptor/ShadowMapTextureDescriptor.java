package com.destrostudios.icetea.core.material.descriptor;

import com.destrostudios.icetea.core.render.shadow.ShadowMapRenderJob;

import static org.lwjgl.vulkan.VK10.VK_IMAGE_LAYOUT_DEPTH_STENCIL_READ_ONLY_OPTIMAL;

public class ShadowMapTextureDescriptor extends TextureDescriptor {

    public ShadowMapTextureDescriptor(String name, ShadowMapRenderJob shadowMapRenderJob) {
        super(name, shadowMapRenderJob.getShadowMapTexture(), VK_IMAGE_LAYOUT_DEPTH_STENCIL_READ_ONLY_OPTIMAL);
    }
}
