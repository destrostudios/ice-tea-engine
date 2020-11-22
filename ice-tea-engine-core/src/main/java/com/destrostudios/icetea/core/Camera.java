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

        transformUniformData = new UniformData();
        transformUniformData.setApplication(application);
        updateProjectionMatrixUniform();
        updateViewMatrixUniform();
        transformUniformData.initBuffer();
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
    private boolean isOutdated_Projection;
    private boolean isOutdated_View;
    @Getter
    private UniformData transformUniformData;

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
        isOutdated_View = true;
    }

    public void setRotation(Vector3fc rotation) {
        this.rotation.set(rotation);
        isOutdated_View = true;
    }

    public void update() {
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
    }

    private void updateViewMatrixUniform() {
        transformUniformData.setMatrix4f("view", viewMatrix);
    }

    private void updateViewProjectionMatrix() {
        viewProjectionMatrix.set(projectionMatrix).mul(viewMatrix);
    }

    public void cleanup() {
        transformUniformData.cleanupBuffer();
    }
}
