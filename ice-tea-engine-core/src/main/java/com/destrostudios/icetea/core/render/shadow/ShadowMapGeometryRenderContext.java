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
        // Make camera available as it might be needed for meshes generated in tesselation shaders
        resourceDescriptorSet.setDescriptor("camera", application.getSceneCamera().getTransformUniformBuffer().getDescriptor("default"));
        resourceDescriptorSet.setDescriptor("shadowInfo", renderJob.getShadowInfoUniformBuffer().getDescriptor("default"));
    }
}
