package com.destrostudios.icetea.core;

import lombok.Getter;

import java.util.List;

public abstract class GeometryRenderContext<RJ extends RenderJob<?>> {

    protected Application application;
    protected RJ renderJob;
    protected Geometry geometry;
    @Getter
    protected MaterialDescriptorSet materialDescriptorSet;
    private Long descriptorPool;
    protected List<Long> descriptorSets;

    public void init(Application application, RJ renderJob, Geometry geometry) {
        this.application = application;
        this.renderJob = renderJob;
        this.geometry = geometry;
    }

    public void recreateDescriptorDependencies() {
        cleanupDescriptorDependencies();
        createDescriptorDependencies();
    }

    public void createDescriptorDependencies() {
        materialDescriptorSet = createMaterialDescriptorSet();
        descriptorPool = materialDescriptorSet.createDescriptorPool();
        descriptorSets = materialDescriptorSet.createDescriptorSets(descriptorPool);
    }

    protected abstract MaterialDescriptorSet createMaterialDescriptorSet();

    public abstract RenderPipeline<RJ> getRenderPipeline();

    public abstract long getDescriptorSet(int commandBufferIndex);

    public void cleanup() {
        cleanupDescriptorDependencies();
    }

    public void cleanupDescriptorDependencies() {
        if (materialDescriptorSet != null) {
            materialDescriptorSet.cleanupDescriptorSets(descriptorPool, descriptorSets);
            materialDescriptorSet.cleanupDescriptorPool(descriptorPool);
            materialDescriptorSet = null;
        }
    }
}
