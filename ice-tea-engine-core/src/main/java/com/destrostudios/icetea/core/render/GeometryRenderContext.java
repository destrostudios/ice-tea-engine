package com.destrostudios.icetea.core.render;

import com.destrostudios.icetea.core.lifecycle.LifecycleObject;
import com.destrostudios.icetea.core.resource.ResourceDescriptorSet;
import com.destrostudios.icetea.core.scene.Geometry;
import lombok.Getter;

public abstract class GeometryRenderContext<RJ extends RenderJob<?>, RP extends RenderPipeline<RJ>> extends LifecycleObject {

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
    protected void init() {
        super.init();
        resourceDescriptorSet = new ResourceDescriptorSet();
    }

    @Override
    protected void update(float tpf) {
        super.update(tpf);
        setDescriptors();
        if (geometry.getMesh().isWereBuffersOutdated() || resourceDescriptorSet.isChanged()) {
            resourceDescriptorSet.onApplied();
            renderPipeline.cleanup();
            application.getSwapChain().setCommandBuffersOutdated();
        }
        renderPipeline.update(application, tpf);
    }

    protected abstract void setDescriptors();

    @Override
    protected void cleanupInternal() {
        renderPipeline.cleanup();
        super.cleanupInternal();
    }
}
