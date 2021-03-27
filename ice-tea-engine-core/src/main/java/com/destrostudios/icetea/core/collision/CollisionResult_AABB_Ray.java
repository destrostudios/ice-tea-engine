package com.destrostudios.icetea.core.collision;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class CollisionResult_AABB_Ray {
    private float tMin;
    private float tMax;
}
