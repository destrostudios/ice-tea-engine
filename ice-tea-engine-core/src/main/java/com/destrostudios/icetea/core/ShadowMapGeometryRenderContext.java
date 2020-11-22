package com.destrostudios.icetea.core;

import com.destrostudios.icetea.core.materials.descriptors.GeometryTransformDescriptor;
import com.destrostudios.icetea.core.materials.descriptors.ShadowMapLightTransformDescriptor;

public class ShadowMapGeometryRenderContext extends GeometryRenderContext<ShadowMapRenderJob> {

    @Override
    protected MaterialDescriptorSet createMaterialDescriptorSet() {
        MaterialDescriptorSetLayout descriptorSetLayout = renderJob.getMaterialDescriptorSetLayout();
        MaterialDescriptorSet descriptorSet = new MaterialDescriptorSet(application, descriptorSetLayout);
        descriptorSet.addDescriptor(new ShadowMapLightTransformDescriptor("light", descriptorSetLayout.getDescriptorLayout(0), renderJob));
        descriptorSet.addDescriptor(new GeometryTransformDescriptor("geometry", descriptorSetLayout.getDescriptorLayout(1), geometry));
        return descriptorSet;
    }

    @Override
    public ShadowMapRenderPipeline getRenderPipeline() {
        return renderJob.getOrCreateRenderPipeline(materialDescriptorSet);
    }

    @Override
    public long getDescriptorSet(int commandBufferIndex) {
        return descriptorSets.get(0);
    }
}
