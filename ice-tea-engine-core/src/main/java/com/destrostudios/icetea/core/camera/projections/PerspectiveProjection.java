package com.destrostudios.icetea.core.camera.projections;

import com.destrostudios.icetea.core.camera.SceneCameraProjection;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.joml.Matrix4f;

@AllArgsConstructor
@Getter
public class PerspectiveProjection extends SceneCameraProjection {

    public PerspectiveProjection(PerspectiveProjection perspectiveProjection) {
        fieldOfViewY = perspectiveProjection.fieldOfViewY;
        aspect = perspectiveProjection.aspect;
        zNear = perspectiveProjection.zNear;
        zFar = perspectiveProjection.zFar;
    }
    private float fieldOfViewY;
    private float aspect;
    private float zNear;
    private float zFar;

    @Override
    public void updateProjectionMatrix(Matrix4f projectionMatrix) {
        projectionMatrix.identity();
        projectionMatrix.perspective(fieldOfViewY, aspect, zNear, zFar, true);
        projectionMatrix.m11(projectionMatrix.m11() * -1);
    }

    @Override
    public PerspectiveProjection clone() {
        return new PerspectiveProjection(this);
    }
}
