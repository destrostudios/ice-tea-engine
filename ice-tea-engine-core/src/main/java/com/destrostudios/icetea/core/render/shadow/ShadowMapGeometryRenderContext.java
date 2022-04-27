package com.destrostudios.icetea.core.render.shadow;

import com.destrostudios.icetea.core.render.EssentialGeometryRenderContext;
import com.destrostudios.icetea.core.scene.Geometry;

public class ShadowMapGeometryRenderContext extends EssentialGeometryRenderContext<ShadowMapRenderJob, ShadowMapRenderPipeline> {

    public ShadowMapGeometryRenderContext(Geometry geometry, ShadowMapRenderJob renderJob) {
        super(geometry, renderJob);
    }

    @Override
    protected ShadowMapRenderPipeline createRenderPipeline() {
        return new ShadowMapRenderPipeline(renderJob, geometry, this);
    }

    @Override
    protected void setDescriptors() {
        resourceDescriptorSet.setDescriptor("camera", renderJob.getLightTransformUniformBuffer().getDescriptor("default"));
    }
}
