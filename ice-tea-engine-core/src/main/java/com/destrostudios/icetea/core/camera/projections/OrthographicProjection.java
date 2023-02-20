package com.destrostudios.icetea.core.camera.projections;

import com.destrostudios.icetea.core.camera.SceneCameraProjection;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.joml.Matrix4f;

@AllArgsConstructor
@Getter
public class OrthographicProjection extends SceneCameraProjection {

    public OrthographicProjection(OrthographicProjection orthographicProjection) {
        minX = orthographicProjection.minX;
        maxX = orthographicProjection.maxX;
        minY = orthographicProjection.minY;
        minZ = orthographicProjection.minZ;
        maxZ = orthographicProjection.maxZ;
    }
    private float minX;
    private float maxX;
    private float minY;
    private float maxY;
    private float minZ;
    private float maxZ;

    @Override
    public void updateProjectionMatrix(Matrix4f projectionMatrix) {
        projectionMatrix.identity();
        projectionMatrix.ortho(minX, maxX, minY, maxY, minZ, maxZ, true);
        projectionMatrix.m11(projectionMatrix.m11() * -1);
    }

    @Override
    public OrthographicProjection clone() {
        return new OrthographicProjection(this);
    }
}
