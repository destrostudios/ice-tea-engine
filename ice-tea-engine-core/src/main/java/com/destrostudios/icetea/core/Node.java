package com.destrostudios.icetea.core;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;

public class Node extends Spatial {

    public Node() {
        children = new LinkedList<>();
    }
    private List<Spatial> children;
    private boolean modified;

    @Override
    public boolean update(Application application) {
        boolean commandBufferOutdated = super.update(application);
        for (Spatial child : children) {
            if (child.update(application)) {
                commandBufferOutdated = true;
            }
        }
        if (modified) {
            commandBufferOutdated = true;
            modified = false;
        }
        return commandBufferOutdated;
    }

    @Override
    protected void updateWorldTransform() {
        super.updateWorldTransform();
        for (Spatial child : children) {
            child.onWorldTransformOutdated();
        }
    }

    public void add(Spatial spatial) {
        spatial.setParent(this);
        children.add(spatial);
        modified = true;
    }

    public void remove(Spatial spatial) {
        spatial.setParent(null);
        children.remove(spatial);
        modified = true;
    }

    public void forEachGeometry(Consumer<Geometry> geometryConsumer) {
        for (Spatial child : children) {
            if (child instanceof Node) {
                Node node = (Node) child;
                node.forEachGeometry(geometryConsumer);
            } else {
                Geometry geometry = (Geometry) child;
                geometryConsumer.accept(geometry);
            }
        }
    }
}
