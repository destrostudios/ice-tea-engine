package com.destrostudios.icetea.core.render.scene;

import com.destrostudios.icetea.core.camera.Camera;
import com.destrostudios.icetea.core.light.Light;
import com.destrostudios.icetea.core.render.EssentialGeometryRenderContext;
import com.destrostudios.icetea.core.render.bucket.BucketRenderer;
import com.destrostudios.icetea.core.render.shadow.ShadowMapRenderJob;
import com.destrostudios.icetea.core.scene.Geometry;

import java.util.List;
import java.util.function.Supplier;

public class SceneGeometryRenderContext extends EssentialGeometryRenderContext<SceneRenderJob, SceneRenderPipeline> {

    public SceneGeometryRenderContext(Geometry geometry, SceneRenderJob renderJob, Supplier<Camera> defaultCameraSupplier, BucketRenderer bucketRenderer) {
        super(geometry, renderJob);
        this.defaultCameraSupplier = defaultCameraSupplier;
        this.bucketRenderer = bucketRenderer;
    }
    private Supplier<Camera> defaultCameraSupplier;
    private BucketRenderer bucketRenderer;

    @Override
    protected SceneRenderPipeline createRenderPipeline() {
        return new SceneRenderPipeline(renderJob, geometry, this);
    }

    @Override
    protected void setDescriptors() {
        Camera forcedCamera = bucketRenderer.getBucket(geometry).getForcedCamera();
        Camera camera = ((forcedCamera != null) ? forcedCamera : defaultCameraSupplier.get());
        resourceDescriptorSet.setDescriptor("camera", camera.getTransformUniformBuffer().getDescriptor("default"));

        // TODO: Handle multiple lights + shadows (use a descriptor binding array)
        List<Light> affectingLights = geometry.getAffectingLights();
        for (Light light : affectingLights) {
            resourceDescriptorSet.setDescriptor("light", light.getUniformBuffer().getDescriptor("default"));
            for (ShadowMapRenderJob shadowMapRenderJob : light.getShadowMapRenderJobs()) {
                resourceDescriptorSet.setDescriptor("shadowMapLight", shadowMapRenderJob.getLightTransformUniformBuffer().getDescriptor("default"));
                resourceDescriptorSet.setDescriptor("shadowMapTexture", shadowMapRenderJob.getShadowMapTexture().getDescriptor("default"));
            }
        }
    }
}
