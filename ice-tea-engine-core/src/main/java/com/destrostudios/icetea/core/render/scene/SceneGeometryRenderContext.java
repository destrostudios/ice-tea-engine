package com.destrostudios.icetea.core.render.scene;

import com.destrostudios.icetea.core.camera.Camera;
import com.destrostudios.icetea.core.light.Light;
import com.destrostudios.icetea.core.material.descriptor.*;
import com.destrostudios.icetea.core.render.EssentialGeometryRenderContext;
import com.destrostudios.icetea.core.render.bucket.BucketRenderer;
import com.destrostudios.icetea.core.render.shadow.ShadowMapRenderJob;

import java.util.List;
import java.util.function.Supplier;

public class SceneGeometryRenderContext extends EssentialGeometryRenderContext<SceneRenderJob> {

    public SceneGeometryRenderContext(Supplier<Camera> defaultCameraSupplier, BucketRenderer bucketRenderer) {
        this.defaultCameraSupplier = defaultCameraSupplier;
        this.bucketRenderer = bucketRenderer;
    }
    private Supplier<Camera> defaultCameraSupplier;
    private BucketRenderer bucketRenderer;
    private SceneRenderPipeline sceneRenderPipeline;

    @Override
    protected void fillMaterialDescriptorSet(MaterialDescriptorSetLayout descriptorSetLayout, MaterialDescriptorSet descriptorSet) {
        super.fillMaterialDescriptorSet(descriptorSetLayout,descriptorSet);

        Camera forcedCamera = bucketRenderer.getBucket(geometry).getForcedCamera();
        Camera camera = ((forcedCamera != null) ? forcedCamera : defaultCameraSupplier.get());
        descriptorSetLayout.addDescriptorLayout(new CameraTransformDescriptorLayout());
        descriptorSet.addDescriptor(new CameraTransformDescriptor("camera", camera));

        // TODO: Handle multiple lights + shadows (use a descriptor binding array)
        List<Light> affectingLights = geometry.getAffectingLights();
        for (Light light : affectingLights) {
            descriptorSetLayout.addDescriptorLayout(new LightDescriptorLayout());
            descriptorSet.addDescriptor(new LightDescriptor("light", light));

            for (ShadowMapRenderJob shadowMapRenderJob : light.getShadowMapRenderJobs()) {
                descriptorSetLayout.addDescriptorLayout(new ShadowMapLightTransformDescriptorLayout());
                descriptorSet.addDescriptor(new ShadowMapLightTransformDescriptor("shadowMapLight", shadowMapRenderJob));

                descriptorSetLayout.addDescriptorLayout(new ShadowMapTextureDescriptorLayout());
                descriptorSet.addDescriptor(new ShadowMapTextureDescriptor("shadowMapTexture", shadowMapRenderJob));
            }
        }
    }

    @Override
    public void createDescriptorDependencies() {
        super.createDescriptorDependencies();
        sceneRenderPipeline = new SceneRenderPipeline(application, renderJob, geometry, this);
        sceneRenderPipeline.init();
    }

    @Override
    public void cleanupDescriptorDependencies() {
        if (materialDescriptorSet != null) {
            materialDescriptorSet.getSetLayout().cleanupDescriptorSetLayout();
        }
        super.cleanupDescriptorDependencies();
        if (sceneRenderPipeline != null) {
            sceneRenderPipeline.cleanup();
            sceneRenderPipeline = null;
        }
    }

    @Override
    public SceneRenderPipeline getRenderPipeline() {
        return sceneRenderPipeline;
    }

    @Override
    public long getDescriptorSet(int commandBufferIndex) {
        return descriptorSets.get(commandBufferIndex);
    }
}
