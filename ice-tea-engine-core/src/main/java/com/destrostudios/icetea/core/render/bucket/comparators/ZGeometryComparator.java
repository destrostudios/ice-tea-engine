package com.destrostudios.icetea.core.render.bucket.comparators;

import com.destrostudios.icetea.core.scene.Geometry;

import java.util.Comparator;

public class ZGeometryComparator implements Comparator<Geometry> {

    @Override
    public int compare(Geometry geometry1, Geometry geometry2) {
        float z1 = geometry1.getWorldTransform().getTranslation().z();
        float z2 = geometry2.getWorldTransform().getTranslation().z();
        if (z1 < z2) {
            return -1;
        }  else if (z1 > z2) {
            return 1;
        }
        return 0;
    }
}
