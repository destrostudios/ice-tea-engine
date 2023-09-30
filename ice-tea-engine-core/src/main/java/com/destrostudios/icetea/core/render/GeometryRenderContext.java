package com.destrostudios.icetea.core.render;

import com.destrostudios.icetea.core.object.NativeObject;
import com.destrostudios.icetea.core.resource.ResourceDescriptorSet;
import com.destrostudios.icetea.core.scene.Geometry;
import lombok.Getter;

public abstract class GeometryRenderContext<RJ extends RenderJob<?>, RP extends RenderPipeline<RJ>> extends NativeObject {

    public GeometryRenderContext(Geometry geometry, RJ renderJob) {
        this.geometry = geometry;
        this.renderJob = renderJob;
        this.renderPipeline = createRenderPipeline();
    }
    protected RJ renderJob;
    @Getter
    private RP renderPipeline;
    @Getter
    protected Geometry geometry;
    @Getter
    protected ResourceDescriptorSet resourceDescriptorSet;

    protected abstract RP createRenderPipeline();

    @Override
    protected void initNative() {
        super.initNative();
        resourceDescriptorSet = new ResourceDescriptorSet();
    }

    @Override
    public void updateNative() {
        super.updateNative();
        setDescriptors();
        if (geometry.getMesh().isWereBuffersOutdated() || resourceDescriptorSet.isChanged()) {
            resourceDescriptorSet.onChangeApplied();
            renderPipeline.cleanupNative();
        }
        renderPipeline.updateNative(application);
    }

    protected abstract void setDescriptors();

    @Override
    protected void cleanupNativeInternal() {
        renderPipeline.cleanupNative();
        super.cleanupNativeInternal();
    }
}
