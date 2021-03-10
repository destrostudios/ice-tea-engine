package com.destrostudios.icetea.core.render.scene.bucket.comparators;

import com.destrostudios.icetea.core.scene.Camera;

public class BackToFrontGeometryComparator extends CameraDistanceGeometryComparator {

    public BackToFrontGeometryComparator(Camera camera) {
        super(camera, false);
    }
}
