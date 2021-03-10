package com.destrostudios.icetea.core.render.scene.bucket.comparators;

import com.destrostudios.icetea.core.scene.Camera;
import com.destrostudios.icetea.core.scene.Geometry;

import java.util.Comparator;

public class CameraDistanceGeometryComparator implements Comparator<Geometry> {

    public CameraDistanceGeometryComparator(Camera camera, boolean prioritizeNearOrFar) {
        this.camera = camera;
        this.prioritizeNearOrFar = prioritizeNearOrFar;
    }
    private Camera camera;
    private boolean prioritizeNearOrFar;

    @Override
    public int compare(Geometry geometry1, Geometry geometry2) {
        float distanceSquared1 = getDistanceSquaredToCamera(geometry1);
        float distanceSquared2 = getDistanceSquaredToCamera(geometry2);
        int result;
        if (distanceSquared1 > distanceSquared2) {
            result = -1;
        }  else if (distanceSquared1 < distanceSquared2) {
            result = 1;
        } else {
            return 0;
        }
        if (prioritizeNearOrFar) {
            result *= -1;
        }
        return result;
    }

    protected float getDistanceSquaredToCamera(Geometry geometry) {
        return geometry.getWorldTransform().getTranslation().distanceSquared(camera.getLocation());
    }
}
