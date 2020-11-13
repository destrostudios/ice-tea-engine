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
    private boolean commandBufferOutdated;

    public void addGeometry(Geometry geometry) {
        geometry.init(application);
        geometries.add(geometry);
        commandBufferOutdated = true;
    }

    public void removeGeometry(Geometry geometry) {
        geometry.cleanup();
        geometries.remove(geometry);
        commandBufferOutdated = true;
    }

    public void update() {
        for (Geometry geometry : geometries) {
            if (geometry.update()) {
                commandBufferOutdated = true;
            }
        }
        if (commandBufferOutdated) {
            application.getSwapChain().recreateCommandBuffers();
            commandBufferOutdated = false;
        }
    }

    public void cleanup() {
        for (Geometry geometry : geometries) {
            geometry.cleanup();
        }
    }
}
