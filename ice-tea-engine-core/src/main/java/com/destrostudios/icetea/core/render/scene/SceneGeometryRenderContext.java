package com.destrostudios.icetea.core.render.scene;

import com.destrostudios.icetea.core.camera.Camera;
import com.destrostudios.icetea.core.material.descriptor.MaterialDescriptorSet;
import com.destrostudios.icetea.core.material.descriptor.MaterialDescriptorSetLayout;
import com.destrostudios.icetea.core.material.descriptor.*;
import com.destrostudios.icetea.core.render.GeometryRenderContext;
import com.destrostudios.icetea.core.render.bucket.BucketRenderer;
import com.destrostudios.icetea.core.render.shadow.ShadowMapRenderJob;
import com.destrostudios.icetea.core.light.Light;
import com.destrostudios.icetea.core.texture.Texture;

import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public class SceneGeometryRenderContext extends GeometryRenderContext<SceneRenderJob> {

    public SceneGeometryRenderContext(Supplier<Camera> defaultCameraSupplier, BucketRenderer bucketRenderer) {
        this.defaultCameraSupplier = defaultCameraSupplier;
        this.bucketRenderer = bucketRenderer;
    }
    private Supplier<Camera> defaultCameraSupplier;
    private BucketRenderer bucketRenderer;
    private SceneRenderPipeline sceneRenderPipeline;

    @Override
    protected MaterialDescriptorSet createMaterialDescriptorSet() {
        MaterialDescriptorSetLayout descriptorSetLayout = new MaterialDescriptorSetLayout(application);
        MaterialDescriptorSet descriptorSet = new MaterialDescriptorSet(application, descriptorSetLayout, application.getSwapChain().getImages().size());

        Camera forcedCamera = bucketRenderer.getBucket(geometry).getForcedCamera();
        Camera camera = ((forcedCamera != null) ? forcedCamera : defaultCameraSupplier.get());
        CameraTransformDescriptorLayout cameraTransformDescriptorLayout = new CameraTransformDescriptorLayout();
        CameraTransformDescriptor cameraTransformDescriptor = new CameraTransformDescriptor("camera", cameraTransformDescriptorLayout, camera);
        descriptorSetLayout.addDescriptorLayout(cameraTransformDescriptorLayout);
        descriptorSet.addDescriptor(cameraTransformDescriptor);

        GeometryTransformDescriptorLayout geometryTransformDescriptorLayout = new GeometryTransformDescriptorLayout();
        GeometryTransformDescriptor geometryTransformDescriptor = new GeometryTransformDescriptor("geometry", geometryTransformDescriptorLayout, geometry);
        descriptorSetLayout.addDescriptorLayout(geometryTransformDescriptorLayout);
        descriptorSet.addDescriptor(geometryTransformDescriptor);

        if (geometry.getMaterial().getParameters().getSize() > 0) {
            MaterialParamsDescriptorLayout materialParamsDescriptorLayout = new MaterialParamsDescriptorLayout();
            MaterialParamsDescriptor materialParamsDescriptor = new MaterialParamsDescriptor("params", materialParamsDescriptorLayout, geometry.getMaterial());
            descriptorSetLayout.addDescriptorLayout(materialParamsDescriptorLayout);
            descriptorSet.addDescriptor(materialParamsDescriptor);
        }

        for (Map.Entry<String, Supplier<Texture>> entry : geometry.getMaterial().getTextureSuppliers().entrySet()) {
            SimpleTextureDescriptorLayout simpleTextureDescriptorLayout = new SimpleTextureDescriptorLayout();
            SimpleTextureDescriptor simpleTextureDescriptor = new SimpleTextureDescriptor(entry.getKey(), simpleTextureDescriptorLayout, entry.getValue().get());
            descriptorSetLayout.addDescriptorLayout(simpleTextureDescriptorLayout);
            descriptorSet.addDescriptor(simpleTextureDescriptor);
        }

        // TODO: Handle multiple lights + shadows (use a descriptor binding array)
        List<Light> affectingLights = geometry.getAffectingLights();
        for (Light light : affectingLights) {
            LightDescriptorLayout lightDescriptorLayout = new LightDescriptorLayout();
            LightDescriptor lightDescriptor = new LightDescriptor("light", lightDescriptorLayout, light);
            descriptorSetLayout.addDescriptorLayout(lightDescriptorLayout);
            descriptorSet.addDescriptor(lightDescriptor);

            for (ShadowMapRenderJob shadowMapRenderJob : light.getShadowMapRenderJobs()) {
                ShadowMapLightTransformDescriptorLayout shadowMapLightTransformDescriptorLayout = new ShadowMapLightTransformDescriptorLayout();
                ShadowMapLightTransformDescriptor shadowMapLightTransformDescriptor = new ShadowMapLightTransformDescriptor("shadowMapLight", shadowMapLightTransformDescriptorLayout, shadowMapRenderJob);
                descriptorSetLayout.addDescriptorLayout(shadowMapLightTransformDescriptorLayout);
                descriptorSet.addDescriptor(shadowMapLightTransformDescriptor);

                ShadowMapTextureDescriptorLayout shadowMapTextureDescriptorLayout = new ShadowMapTextureDescriptorLayout();
                ShadowMapTextureDescriptor shadowMapTextureDescriptor = new ShadowMapTextureDescriptor("shadowMapTexture", shadowMapTextureDescriptorLayout, shadowMapRenderJob);
                descriptorSetLayout.addDescriptorLayout(shadowMapTextureDescriptorLayout);
                descriptorSet.addDescriptor(shadowMapTextureDescriptor);
            }
        }

        descriptorSetLayout.initDescriptorSetLayout();

        return descriptorSet;
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
