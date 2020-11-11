package com.destrostudios.icetea.core;

import lombok.Getter;

import java.util.LinkedList;
import java.util.List;

public class SceneGraph {

    public SceneGraph(Application application) {
        this.application = application;
        geometries = new LinkedList<>();
    }
    private Application application;
    @Getter
    private List<Geometry> geometries;
    private boolean modified;

    public void addGeometry(Geometry geometry) {
        geometry.init(application);
        geometries.add(geometry);
        modified = true;
    }

    public void update() {
        for (Geometry geometry : geometries) {
            geometry.update();
        }
        if (modified) {
            application.getSwapChain().recreateCommandBuffers();
            modified = false;
        }
    }

    public void cleanup() {
        for (Geometry geometry : geometries) {
            geometry.cleanup();
        }
    }
}
