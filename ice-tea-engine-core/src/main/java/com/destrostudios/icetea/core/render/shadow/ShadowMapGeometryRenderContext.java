package com.destrostudios.icetea.core.render.shadow;

import com.destrostudios.icetea.core.material.descriptor.MaterialDescriptorSet;
import com.destrostudios.icetea.core.material.descriptor.MaterialDescriptorSetLayout;
import com.destrostudios.icetea.core.material.descriptor.ShadowMapLightTransformDescriptor;
import com.destrostudios.icetea.core.material.descriptor.ShadowMapLightTransformDescriptorLayout;
import com.destrostudios.icetea.core.render.EssentialGeometryRenderContext;

public class ShadowMapGeometryRenderContext extends EssentialGeometryRenderContext<ShadowMapRenderJob> {

    private ShadowMapRenderPipeline shadowMapRenderPipeline;

    @Override
    protected void fillMaterialDescriptorSet(MaterialDescriptorSetLayout descriptorSetLayout, MaterialDescriptorSet descriptorSet) {
        super.fillMaterialDescriptorSet(descriptorSetLayout,descriptorSet);

        descriptorSetLayout.addDescriptorLayout(new ShadowMapLightTransformDescriptorLayout());
        descriptorSet.addDescriptor(new ShadowMapLightTransformDescriptor("camera", renderJob));
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
        return descriptorSets.get(commandBufferIndex);
    }
}
