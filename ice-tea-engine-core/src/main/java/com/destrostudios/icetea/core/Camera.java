package com.destrostudios.icetea.core;

import lombok.Getter;
import org.joml.*;

public class Camera {

    public Camera(Application application) {
        location = new Vector3f();
        rotation = new Vector3f();

        projectionMatrix = new Matrix4f();
        viewMatrix = new Matrix4f();
        viewProjectionMatrix = new Matrix4f();

        clipPlane = new Vector4f();

        transformUniformData = new UniformData();
        transformUniformData.setApplication(application);
        updateLocationUniform();
        updateRotationUniform();
        updateProjectionMatrixUniform();
        updateViewMatrixUniform();
        updateClipPlaneUniform();
        transformUniformData.initBuffers(application.getSwapChain().getImages().size());
    }
    @Getter
    private float fieldOfViewY;
    @Getter
    private float aspect;
    @Getter
    private float zNear;
    @Getter
    private float zFar;
    @Getter
    private Vector3f location;
    @Getter
    private Vector3f rotation;
    @Getter
    private Matrix4f projectionMatrix;
    @Getter
    private Matrix4f viewMatrix;
    @Getter
    private Matrix4f viewProjectionMatrix;
    @Getter
    private Vector4f clipPlane;
    private boolean isOutdated_Location;
    private boolean isOutdated_Rotation;
    private boolean isOutdated_Projection;
    private boolean isOutdated_View;
    private boolean isOutdated_ClipPlane;
    @Getter
    private UniformData transformUniformData;

    public void set(Camera camera) {
        fieldOfViewY = camera.getFieldOfViewY();
        aspect = camera.getAspect();
        zNear = camera.getZNear();
        zFar = camera.getZFar();
        location.set(camera.getLocation());
        rotation.set(camera.getRotation());
        projectionMatrix.set(camera.getProjectionMatrix());
        viewMatrix.set(camera.getViewMatrix());
        viewProjectionMatrix.set(camera.getViewProjectionMatrix());
        clipPlane.set(camera.getClipPlane());
        isOutdated_Location = false;
        isOutdated_Rotation = false;
        isOutdated_Projection = false;
        isOutdated_View = false;
        isOutdated_ClipPlane = false;
        updateLocationUniform();
        updateRotationUniform();
        updateProjectionMatrixUniform();
        updateViewMatrixUniform();
        updateClipPlaneUniform();
    }

    public void setFieldOfViewY(float fieldOfViewY) {
        this.fieldOfViewY = fieldOfViewY;
        isOutdated_Projection = true;
    }

    public void setAspect(float aspect) {
        this.aspect = aspect;
        isOutdated_Projection = true;
    }

    public void setZFar(float zFar) {
        this.zFar = zFar;
        isOutdated_Projection = true;
    }

    public void setZNear(float zNear) {
        this.zNear = zNear;
        isOutdated_Projection = true;
    }

    public void setLocation(Vector3f location) {
        this.location.set(location);
        isOutdated_Location = true;
        isOutdated_View = true;
    }

    public void setRotation(Vector3fc rotation) {
        this.rotation.set(rotation);
        isOutdated_Rotation = true;
        isOutdated_View = true;
    }

    public void setClipPlane(Vector4f clipPlane) {
        this.clipPlane = clipPlane;
        isOutdated_ClipPlane = true;
    }

    public void update() {
        if (isOutdated_Location) {
            updateLocationUniform();
            isOutdated_Location = false;
        }
        if (isOutdated_Rotation) {
            updateRotationUniform();
            isOutdated_Rotation = false;
        }
        if (isOutdated_Projection || isOutdated_View) {
            if (isOutdated_Projection) {
                updateProjectionMatrix();
                isOutdated_Projection = false;
            }
            if (isOutdated_View) {
                updateViewMatrix();
                isOutdated_View = false;
            }
            updateViewProjectionMatrix();
        }
        if (isOutdated_ClipPlane) {
            updateClipPlaneUniform();
            isOutdated_ClipPlane = false;
        }
    }

    private void updateLocationUniform() {
        transformUniformData.setVector3f("location", location);
    }

    private void updateRotationUniform() {
        transformUniformData.setVector3f("rotation", rotation);
    }

    private void updateProjectionMatrix() {
        projectionMatrix.perspective(fieldOfViewY, aspect, zNear, zFar, true);
        projectionMatrix.m11(projectionMatrix.m11() * -1);
        updateProjectionMatrixUniform();
    }

    private void updateProjectionMatrixUniform() {
        transformUniformData.setMatrix4f("proj", projectionMatrix);
    }

    private void updateViewMatrix() {
        MathUtil.setViewMatrix(viewMatrix, location, rotation);
        updateViewMatrixUniform();
    }

    private void updateViewMatrixUniform() {
        transformUniformData.setMatrix4f("view", viewMatrix);
    }

    private void updateViewProjectionMatrix() {
        viewProjectionMatrix.set(projectionMatrix).mul(viewMatrix);
    }

    private void updateClipPlaneUniform() {
        transformUniformData.setVector4f("clipPlane", clipPlane);
    }

    public void cleanup() {
        transformUniformData.cleanupBuffer();
    }
}
