package com.destrostudios.icetea.core.render.scene.bucket;

import com.destrostudios.icetea.core.scene.Geometry;
import lombok.Getter;

import java.util.Comparator;
import java.util.LinkedList;
import java.util.function.Consumer;

public class RenderBucket {

    public RenderBucket(Comparator<Geometry> comparator) {
        this.comparator = comparator;
        geometries = new LinkedList<>();
    }
    @Getter
    private Comparator<Geometry> comparator;
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
