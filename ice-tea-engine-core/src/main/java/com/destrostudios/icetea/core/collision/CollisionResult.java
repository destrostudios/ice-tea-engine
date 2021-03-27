package com.destrostudios.icetea.core.collision;

import com.destrostudios.icetea.core.scene.Geometry;
import lombok.Getter;
import lombok.Setter;
import org.joml.Vector3f;

@Getter
public class CollisionResult {

    public CollisionResult(Vector3f position, Vector3f normal, float distance, int triangleIndex) {
        this.position = position;
        this.normal = normal;
        this.distance = distance;
        this.triangleIndex = triangleIndex;
    }
    private Vector3f position;
    private Vector3f normal;
    private float distance;
    private int triangleIndex;
    @Setter
    private Geometry geometry;
}
