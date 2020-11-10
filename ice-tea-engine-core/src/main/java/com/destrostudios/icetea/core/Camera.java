package com.destrostudios.icetea.core;

import lombok.Getter;
import org.joml.*;

public class Camera {

    public Camera() {
        location = new Vector3f();
        direction = new Vector3f(0, 1, 0);

        viewMatrix = new Matrix4f();
        projectionMatrix = new Matrix4f();
        viewProjectionMatrix = new Matrix4f();
    }
    @Getter
    private Vector3f location;
    @Getter
    private Vector3f direction;
    @Getter
    private Matrix4f viewMatrix;
    @Getter
    private Matrix4f projectionMatrix;
    @Getter
    private Matrix4f viewProjectionMatrix;
    @Getter
    private float fieldOfViewY;
    @Getter
    private float aspect;
    @Getter
    private float zNear;
    @Getter
    private float zFar;
    @Getter
    private boolean isOutdated_View;
    private boolean isOutdated_Projection;

    public void setLocation(Vector3f location) {
        this.location.set(location);
        isOutdated_View = true;
    }

    public void setDirection(Vector3fc direction) {
        this.direction.set(direction);
        isOutdated_View = true;
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

    public void update() {
        if (isOutdated_View || isOutdated_Projection) {
            if (isOutdated_View) {
                updateViewMatrix();
                isOutdated_View = false;
            }
            if (isOutdated_Projection) {
                updateProjectionMatrix();
                isOutdated_Projection = false;
            }
            updateViewProjectionMatrix();
        }
    }

    public void updateViewMatrix() {
        viewMatrix.lookAt(
            location.x(), location.y(), location.z(),
            location.x() + direction.x(), location.y() + direction.y(), location.z() + direction.z(),
            0, 0, 1
        );
    }

    public void updateProjectionMatrix() {
        projectionMatrix.perspective(fieldOfViewY, aspect, zNear, zFar);
        projectionMatrix.m11(projectionMatrix.m11() * -1);
    }

    public void updateViewProjectionMatrix() {
        viewProjectionMatrix.set(projectionMatrix).mul(viewMatrix);
    }
}
