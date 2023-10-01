package com.destrostudios.icetea.core.render.bucket;

import com.destrostudios.icetea.core.camera.Camera;
import com.destrostudios.icetea.core.scene.Geometry;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Comparator;
import java.util.List;

@AllArgsConstructor
public class RenderBucket {

    public RenderBucket(Comparator<Geometry> comparator) {
        this(comparator, null);
    }
    private Comparator<Geometry> comparator;
    @Getter
    private Camera forcedCamera;

    public void sort(List<Geometry> geometries) {
        geometries.sort(comparator);
    }
}
