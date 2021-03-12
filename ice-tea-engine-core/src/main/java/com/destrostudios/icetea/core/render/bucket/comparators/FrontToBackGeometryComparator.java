package com.destrostudios.icetea.core.render.bucket.comparators;

import com.destrostudios.icetea.core.camera.Camera;

public class FrontToBackGeometryComparator extends CameraDistanceGeometryComparator {

    public FrontToBackGeometryComparator(Camera camera) {
        super(camera, true);
    }
}
