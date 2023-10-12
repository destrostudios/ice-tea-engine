package com.destrostudios.icetea.core.render;

import com.destrostudios.icetea.core.Pipeline;
import com.destrostudios.icetea.core.object.NativeObject;
import com.destrostudios.icetea.core.resource.ResourceDescriptorSet;
import com.destrostudios.icetea.core.scene.Geometry;
import lombok.Getter;

public abstract class GeometryRenderContext<RJ extends RenderJob<?, ?>> extends NativeObject {

    public GeometryRenderContext(Geometry geometry, RJ renderJob) {
        this.geometry = geometry;
        this.renderJob = renderJob;
    }
    protected RJ renderJob;
    @Getter
    protected Geometry geometry;
    @Getter
    protected ResourceDescriptorSet resourceDescriptorSet;
    @Getter
    private Pipeline renderPipeline;

    @Override
    protected void initNative() {
        super.initNative();
        resourceDescriptorSet = new ResourceDescriptorSet();
    }

    @Override
    public void updateNative() {
        super.updateNative();
        setDescriptors();
        renderPipeline = renderJob.getRenderPipelineCreator().getOrCreatePipeline(this);
        renderPipeline.updateNative(application);
    }

    protected abstract void setDescriptors();

    @Override
    protected void cleanupNativeInternal() {
        // Don't cleanup (the potentially shared) renderPipeline to keep it in the PipelineManager cache (which owns and controls its lifetime)
        super.cleanupNativeInternal();
    }
}
