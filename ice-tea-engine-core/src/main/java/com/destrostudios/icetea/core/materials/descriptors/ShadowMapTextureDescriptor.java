package com.destrostudios.icetea.core.materials.descriptors;

import com.destrostudios.icetea.core.ShadowMapRenderJob;

import static org.lwjgl.vulkan.VK10.VK_IMAGE_LAYOUT_DEPTH_STENCIL_READ_ONLY_OPTIMAL;

public class ShadowMapTextureDescriptor extends TextureDescriptor<ShadowMapTextureDescriptorLayout> {

    public ShadowMapTextureDescriptor(String name, ShadowMapTextureDescriptorLayout layout, ShadowMapRenderJob shadowMapRenderJob) {
        super(name, layout, shadowMapRenderJob.getShadowMapTexture(), VK_IMAGE_LAYOUT_DEPTH_STENCIL_READ_ONLY_OPTIMAL);
    }
}