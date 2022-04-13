package com.destrostudios.icetea.core.camera;

import com.destrostudios.icetea.core.Application;
import com.destrostudios.icetea.core.data.UniformData;
import lombok.Getter;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;

public class Camera {

    public Camera() {
        location = new Vector3f();
        projectionMatrix = new Matrix4f();
        viewMatrix = new Matrix4f();
        projectionViewMatrix = new Matrix4f();
        clipPlane = new Vector4f();
        transformUniformData = new UniformData();
    }
    @Getter
    protected Vector3f location;
    @Getter
    protected Matrix4f projectionMatrix;
    @Getter
    protected Matrix4f viewMatrix;
    @Getter
    protected Matrix4f projectionViewMatrix;
    @Getter
    protected Vector4f clipPlane;
    @Getter
    private UniformData transformUniformData;

    public void init(Application application) {
        transformUniformData.setApplication(application);
        updateUniform_Location();
        updateUniform_ProjectionMatrix();
        updateUniform_ViewMatrix();
        updateUniform_ClipPlane();
        transformUniformData.initBuffers(application.getSwapChain().getImages().size());
    }

    protected void set(Camera camera) {
        location.set(camera.getLocation());
        projectionMatrix.set(camera.getProjectionMatrix());
        viewMatrix.set(camera.getViewMatrix());
        projectionViewMatrix.set(camera.getProjectionViewMatrix());
        clipPlane.set(camera.getClipPlane());
        updateUniform_Location();
        updateUniform_ProjectionMatrix();
        updateUniform_ViewMatrix();
        updateUniform_ClipPlane();
    }

    protected void updateProjectionViewMatrix() {
        projectionViewMatrix.set(projectionMatrix).mul(viewMatrix);
    }

    protected void updateUniform_Location() {
        transformUniformData.setVector3f("location", location);
    }

    protected void updateUniform_ProjectionMatrix() {
        transformUniformData.setMatrix4f("proj", projectionMatrix);
    }

    protected void updateUniform_ViewMatrix() {
        transformUniformData.setMatrix4f("view", viewMatrix);
    }

    protected void updateUniform_ClipPlane() {
        transformUniformData.setVector4f("clipPlane", clipPlane);
    }

    public void updateUniformBuffers(int currentImage) {
        transformUniformData.updateBufferIfNecessary(currentImage);
    }

    public void cleanup() {
        transformUniformData.cleanupBuffer();
    }
}
