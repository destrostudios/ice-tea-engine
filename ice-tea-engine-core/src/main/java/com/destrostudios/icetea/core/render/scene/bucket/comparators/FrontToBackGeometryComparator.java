package com.destrostudios.icetea.core.render.scene.bucket.comparators;

import com.destrostudios.icetea.core.scene.Camera;

public class FrontToBackGeometryComparator extends CameraDistanceGeometryComparator {

    public FrontToBackGeometryComparator(Camera camera) {
        super(camera, true);
    }
}
