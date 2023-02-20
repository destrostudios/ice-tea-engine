package com.destrostudios.icetea.core.camera.projections;

import com.destrostudios.icetea.core.camera.SceneCameraProjection;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.joml.Matrix4f;

@AllArgsConstructor
@Getter
public class OrthographicProjection extends SceneCameraProjection {

    public OrthographicProjection(OrthographicProjection orthographicProjection) {
        left = orthographicProjection.left;
        right = orthographicProjection.right;
        bottom = orthographicProjection.bottom;
        top = orthographicProjection.top;
    }
    private float left;
    private float right;
    private float bottom;
    private float top;

    @Override
    public void updateProjectionMatrix(Matrix4f projectionMatrix, float zNear, float zFar) {
        projectionMatrix.identity();
        projectionMatrix.ortho(left, right, bottom, top, zNear, zFar, true);
        projectionMatrix.m11(projectionMatrix.m11() * -1);
    }

    @Override
    public OrthographicProjection clone() {
        return new OrthographicProjection(this);
    }
}
