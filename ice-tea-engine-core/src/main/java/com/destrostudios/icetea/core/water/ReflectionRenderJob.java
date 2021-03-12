package com.destrostudios.icetea.core.water;

import com.destrostudios.icetea.core.*;
import com.destrostudios.icetea.core.camera.SceneCamera;
import com.destrostudios.icetea.core.render.scene.SceneGeometryRenderContext;
import com.destrostudios.icetea.core.render.scene.SceneRenderJob;
import com.destrostudios.icetea.core.scene.Geometry;
import org.joml.Vector3f;
import org.joml.Vector4f;

public class ReflectionRenderJob extends SceneRenderJob {

    public ReflectionRenderJob(Geometry geometryWater) {
        this.geometryWater = geometryWater;
    }
    private Geometry geometryWater;
    private SceneCamera reflectionCamera;

    @Override
    public void init(Application application) {
        super.init(application);
        reflectionCamera = new SceneCamera();
        reflectionCamera.init(application);
    }

    @Override
    public boolean isRendering(Geometry geometry) {
        return ((geometry != geometryWater) && geometry.hasParent(application.getSceneNode()));
    }

    @Override
    public SceneGeometryRenderContext createGeometryRenderContext() {
        return new SceneGeometryRenderContext(() -> reflectionCamera, application.getBucketRenderer());
    }

    @Override
    public void updateUniformBuffers(int currentImage) {
        super.updateUniformBuffers(currentImage);
        reflectionCamera.set(application.getSceneCamera());
        Vector3f location = reflectionCamera.getLocation();
        float waterHeight = 0;
        float distance = (2 * (location.z() - waterHeight));
        location.setComponent(2, location.z() - distance);
        reflectionCamera.setLocation(location);
        Vector3f rotation = reflectionCamera.getRotation();
        // Invert pitch (TODO: Better camera rotation vector)
        rotation.setComponent(0, rotation.x() + (2 * (-90 - rotation.x())));
        reflectionCamera.setRotation(rotation);
        reflectionCamera.setClipPlane(new Vector4f(0, 0, 1, waterHeight));
        reflectionCamera.update();
        reflectionCamera.getTransformUniformData().updateBufferIfNecessary(currentImage);
    }

    @Override
    public void cleanup() {
        if (isInitialized()) {
            reflectionCamera.cleanup();
            reflectionCamera = null;
        }
        super.cleanup();
    }
}
