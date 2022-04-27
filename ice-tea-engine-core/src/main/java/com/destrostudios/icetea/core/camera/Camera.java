package com.destrostudios.icetea.core.camera;

import com.destrostudios.icetea.core.buffer.UniformDataBuffer;
import com.destrostudios.icetea.core.lifecycle.LifecycleObject;
import com.destrostudios.icetea.core.resource.descriptor.CameraTransformDescriptor;
import lombok.Getter;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;

public class Camera extends LifecycleObject {

    public Camera() {
        location = new Vector3f();
        projectionMatrix = new Matrix4f();
        viewMatrix = new Matrix4f();
        projectionViewMatrix = new Matrix4f();
        clipPlane = new Vector4f();
        transformUniformBuffer = new UniformDataBuffer();
        transformUniformBuffer.setDescriptor("default", new CameraTransformDescriptor());
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
    private UniformDataBuffer transformUniformBuffer;

    @Override
    protected void init() {
        super.init();
        updateUniform_Location();
        updateUniform_ProjectionMatrix();
        updateUniform_ViewMatrix();
        updateUniform_ClipPlane();
    }

    @Override
    public void update(float tpf) {
        super.update(tpf);
        application.getSwapChain().setResourceActive(transformUniformBuffer);
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
        transformUniformBuffer.getData().setVector3f("location", location);
    }

    protected void updateUniform_ProjectionMatrix() {
        transformUniformBuffer.getData().setMatrix4f("proj", projectionMatrix);
    }

    protected void updateUniform_ViewMatrix() {
        transformUniformBuffer.getData().setMatrix4f("view", viewMatrix);
    }

    protected void updateUniform_ClipPlane() {
        transformUniformBuffer.getData().setVector4f("clipPlane", clipPlane);
    }

    @Override
    protected void cleanupInternal() {
        transformUniformBuffer.cleanup();
        super.cleanupInternal();
    }
}
