package com.destrostudios.icetea.core.camera;

import com.destrostudios.icetea.core.util.MathUtil;
import lombok.Getter;
import org.joml.Matrix3f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.joml.Vector4f;

public class SceneCamera extends Camera {

    public SceneCamera() {
        rotation = new Quaternionf();
    }
    @Getter
    private Quaternionf rotation;
    @Getter
    private float fieldOfViewY;
    @Getter
    private float aspect;
    @Getter
    private float zNear;
    @Getter
    private float zFar;

    public void set(SceneCamera sceneCamera) {
        super.set(sceneCamera);
        rotation.set(sceneCamera.getRotation());
        fieldOfViewY = sceneCamera.getFieldOfViewY();
        aspect = sceneCamera.getAspect();
        zNear = sceneCamera.getZNear();
        zFar = sceneCamera.getZFar();
    }

    public void setFieldOfViewY(float fieldOfViewY) {
        this.fieldOfViewY = fieldOfViewY;
        updateProjectionMatrix();
    }

    public void setAspect(float aspect) {
        this.aspect = aspect;
        updateProjectionMatrix();
    }

    public void setZFar(float zFar) {
        this.zFar = zFar;
        updateProjectionMatrix();
    }

    public void setZNear(float zNear) {
        this.zNear = zNear;
        updateProjectionMatrix();
    }

    public void setLocation(Vector3f location) {
        this.location.set(location);
        updateViewMatrix();
        updateUniform_Location();
    }

    public void setRotation(Quaternionf rotation) {
        this.rotation.set(rotation);
        updateViewMatrix();
    }

    public void setClipPlane(Vector4f clipPlane) {
        this.clipPlane = clipPlane;
        updateUniform_ClipPlane();
    }

    private void updateProjectionMatrix() {
        projectionMatrix.identity();
        projectionMatrix.perspective(fieldOfViewY, aspect, zNear, zFar, true);
        projectionMatrix.m11(projectionMatrix.m11() * -1);
        updateProjectionViewMatrix();
        updateUniform_ProjectionMatrix();
    }

    private void updateViewMatrix() {
        MathUtil.setViewMatrix(viewMatrix, location, rotation);
        updateProjectionViewMatrix();
        updateUniform_ViewMatrix();
    }

    public Vector3f getRight() {
        // TODO: Introduce TempVars
        return rotation.get(new Matrix3f()).getRow(0, new Vector3f());
    }

    public Vector3f getUp() {
        // TODO: Introduce TempVars
        return rotation.get(new Matrix3f()).getRow(1, new Vector3f());
    }

    public Vector3f getBack() {
        // TODO: Introduce TempVars
        return rotation.get(new Matrix3f()).getRow(2, new Vector3f());
    }
}
