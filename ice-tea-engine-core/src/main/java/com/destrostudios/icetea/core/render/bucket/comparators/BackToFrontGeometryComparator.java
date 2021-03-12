package com.destrostudios.icetea.core.render.bucket.comparators;

import com.destrostudios.icetea.core.camera.Camera;

public class BackToFrontGeometryComparator extends CameraDistanceGeometryComparator {

    public BackToFrontGeometryComparator(Camera camera) {
        super(camera, false);
    }
}
