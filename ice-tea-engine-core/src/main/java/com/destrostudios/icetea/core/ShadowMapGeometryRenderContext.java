package com.destrostudios.icetea.core;

import com.destrostudios.icetea.core.materials.descriptors.GeometryTransformDescriptor;
import com.destrostudios.icetea.core.materials.descriptors.ShadowMapLightTransformDescriptor;

public class ShadowMapGeometryRenderContext extends GeometryRenderContext<ShadowMapRenderJob> {

    private ShadowMapRenderPipeline shadowMapRenderPipeline;

    @Override
    protected MaterialDescriptorSet createMaterialDescriptorSet() {
        MaterialDescriptorSetLayout descriptorSetLayout = renderJob.getMaterialDescriptorSetLayout();
        MaterialDescriptorSet descriptorSet = new MaterialDescriptorSet(application, descriptorSetLayout, application.getSwapChain().getImages().size());
        descriptorSet.addDescriptor(new ShadowMapLightTransformDescriptor("light", descriptorSetLayout.getDescriptorLayout(0), renderJob));
        descriptorSet.addDescriptor(new GeometryTransformDescriptor("geometry", descriptorSetLayout.getDescriptorLayout(1), geometry));
        return descriptorSet;
    }

    @Override
    public void createDescriptorDependencies() {
        super.createDescriptorDependencies();
        shadowMapRenderPipeline = new ShadowMapRenderPipeline(application, renderJob, geometry, this);
        shadowMapRenderPipeline.init();
    }

    @Override
    public void cleanupDescriptorDependencies() {
        super.cleanupDescriptorDependencies();
        if (shadowMapRenderPipeline != null) {
            shadowMapRenderPipeline.cleanup();
            shadowMapRenderPipeline = null;
        }
    }

    @Override
    public ShadowMapRenderPipeline getRenderPipeline() {
        return shadowMapRenderPipeline;
    }

    @Override
    public long getDescriptorSet(int commandBufferIndex) {
        return descriptorSets.get(0);
    }
}
