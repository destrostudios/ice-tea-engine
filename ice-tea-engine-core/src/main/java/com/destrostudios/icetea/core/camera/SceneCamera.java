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
    private boolean isOutdated_Location;
    private boolean isOutdated_Projection;
    private boolean isOutdated_View;
    private boolean isOutdated_ClipPlane;

    public void set(SceneCamera sceneCamera) {
        super.set(sceneCamera);
        fieldOfViewY = sceneCamera.getFieldOfViewY();
        aspect = sceneCamera.getAspect();
        zNear = sceneCamera.getZNear();
        zFar = sceneCamera.getZFar();
        rotation.set(sceneCamera.getRotation());
        isOutdated_Location = false;
        isOutdated_Projection = false;
        isOutdated_View = false;
        isOutdated_ClipPlane = false;
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

    public void setRotation(Quaternionf rotation) {
        this.rotation.set(rotation);
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
        if (isOutdated_Projection || isOutdated_View) {
            if (isOutdated_Projection) {
                updateProjectionMatrix();
                isOutdated_Projection = false;
            }
            if (isOutdated_View) {
                updateViewMatrix();
                isOutdated_View = false;
            }
            updateProjectionViewMatrix();
        }
        if (isOutdated_ClipPlane) {
            updateClipPlaneUniform();
            isOutdated_ClipPlane = false;
        }
    }

    private void updateProjectionMatrix() {
        projectionMatrix.perspective(fieldOfViewY, aspect, zNear, zFar, true);
        projectionMatrix.m11(projectionMatrix.m11() * -1);
        updateProjectionMatrixUniform();
    }

    private void updateViewMatrix() {
        MathUtil.setViewMatrix(viewMatrix, location, rotation);
        updateViewMatrixUniform();
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
