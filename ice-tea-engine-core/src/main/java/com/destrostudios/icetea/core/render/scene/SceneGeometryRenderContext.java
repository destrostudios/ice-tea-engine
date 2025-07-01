package com.destrostudios.icetea.core.render.scene;

import com.destrostudios.icetea.core.camera.Camera;
import com.destrostudios.icetea.core.light.Light;
import com.destrostudios.icetea.core.render.EssentialGeometryRenderContext;
import com.destrostudios.icetea.core.render.bucket.BucketRenderer;
import com.destrostudios.icetea.core.render.shadow.ShadowMapRenderJob;
import com.destrostudios.icetea.core.resource.ResourceReusability;
import com.destrostudios.icetea.core.scene.Geometry;

import java.util.function.Supplier;

public class SceneGeometryRenderContext extends EssentialGeometryRenderContext<SceneRenderJob> {

    public SceneGeometryRenderContext(Geometry geometry, SceneRenderJob renderJob, Supplier<Camera> defaultCameraSupplier, BucketRenderer bucketRenderer) {
        super(geometry, renderJob);
        this.defaultCameraSupplier = defaultCameraSupplier;
        this.bucketRenderer = bucketRenderer;
    }
    private Supplier<Camera> defaultCameraSupplier;
    private BucketRenderer bucketRenderer;

    @Override
    protected void setDescriptors() {
        super.setDescriptors();
        Camera forcedCamera = bucketRenderer.getBucket(geometry).getForcedCamera();
        Camera camera = ((forcedCamera != null) ? forcedCamera : defaultCameraSupplier.get());
        resourceDescriptorSet.setDescriptor("camera", camera.getTransformUniformBuffer().getDescriptor("default"), ResourceReusability.HIGH);

        if (geometry.isAffectedByLight()) {
            Light light = application.getLight();
            resourceDescriptorSet.setDescriptor("light", light.getUniformBuffer().getDescriptor("default"), ResourceReusability.HIGH);
            ShadowMapRenderJob shadowMapRenderJob = light.getShadowMapRenderJob();
            if (shadowMapRenderJob != null) {
                resourceDescriptorSet.setDescriptor("shadowInfo", shadowMapRenderJob.getShadowInfoUniformBuffer().getDescriptor("default"), ResourceReusability.HIGH);
                resourceDescriptorSet.setDescriptor("shadowMap", shadowMapRenderJob.getShadowMapTexture().getDescriptor("default"), ResourceReusability.HIGH);
            }
        }
    }
}
