package com.destrostudios.icetea.core.render.bucket;

import com.destrostudios.icetea.core.camera.Camera;
import com.destrostudios.icetea.core.scene.Geometry;
import lombok.Getter;

import java.util.Comparator;
import java.util.LinkedList;
import java.util.function.Consumer;

public class RenderBucket {

    public RenderBucket(Comparator<Geometry> comparator) {
        this(comparator, null);
    }

    public RenderBucket(Comparator<Geometry> comparator, Camera forcedCamera) {
        this.comparator = comparator;
        this.forcedCamera = forcedCamera;
        geometries = new LinkedList<>();
    }
    @Getter
    private Comparator<Geometry> comparator;
    @Getter
    private Camera forcedCamera;
    private LinkedList<Geometry> geometries;

    public void clear() {
        geometries.clear();
    }

    public void add(Geometry geometry) {
        geometries.add(geometry);
    }

    public void sort() {
        geometries.sort(comparator);
    }

    public void forEach(Consumer<Geometry> geometryConsumer) {
        geometries.forEach(geometryConsumer);
    }
}
