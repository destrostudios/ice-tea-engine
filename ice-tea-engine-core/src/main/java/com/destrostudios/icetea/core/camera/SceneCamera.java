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
        zNear = 0.1f;
        zFar = 100;
    }
    @Getter
    private Quaternionf rotation;
    @Getter
    private float zNear;
    @Getter
    private float zFar;
    @Getter
    private SceneCameraProjection projection;

    public void set(SceneCamera sceneCamera) {
        super.set(sceneCamera);
        rotation.set(sceneCamera.getRotation());
        zNear = sceneCamera.getZNear();
        zFar = sceneCamera.getZFar();
        projection = sceneCamera.getProjection().clone();
    }

    public void setLocation(Vector3f location) {
        this.location.set(location);
        updateViewMatrix();
        updateUniform_Location();
    }

    public void setClipPlane(Vector4f clipPlane) {
        this.clipPlane = clipPlane;
        updateUniform_ClipPlane();
    }

    public void setRotation(Quaternionf rotation) {
        this.rotation.set(rotation);
        updateViewMatrix();
    }

    public void setZFar(float zFar) {
        this.zFar = zFar;
        updateProjectionMatrix();
    }

    public void setZNear(float zNear) {
        this.zNear = zNear;
        updateProjectionMatrix();
    }

    public void setProjection(SceneCameraProjection projection) {
        this.projection = projection;
        updateProjectionMatrix();
    }

    private void updateProjectionMatrix() {
        projection.updateProjectionMatrix(projectionMatrix, zNear, zFar);
        updateUniform_ProjectionMatrix();
        updateProjectionViewMatrix();
    }

    private void updateViewMatrix() {
        MathUtil.setViewMatrix(viewMatrix, location, rotation);
        updateUniform_ViewMatrix();
        updateProjectionViewMatrix();
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
