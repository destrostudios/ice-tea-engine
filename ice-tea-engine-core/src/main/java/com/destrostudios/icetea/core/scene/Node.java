package com.destrostudios.icetea.core.scene;

import com.destrostudios.icetea.core.Application;
import com.destrostudios.icetea.core.collision.CollisionResult;
import com.destrostudios.icetea.core.collision.Ray;
import com.destrostudios.icetea.core.util.MathUtil;
import lombok.Getter;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;

public class Node extends Spatial {

    public Node() {
        children = new LinkedList<>();
    }
    @Getter
    private List<Spatial> children;
    private boolean modified;

    @Override
    public boolean update(Application application, float tpf) {
        boolean commandBufferOutdated = super.update(application, tpf);
        for (Spatial child : children) {
            if (child.update(application, tpf)) {
                commandBufferOutdated = true;
            }
        }
        updateWorldBoundsIfNecessary();
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
            child.setWorldTransformOutdated();
        }
    }

    @Override
    protected void updateWorldBounds() {
        // TODO: Introduce TempVars
        Vector3f min = new Vector3f(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY);
        Vector3f max = new Vector3f(Float.NEGATIVE_INFINITY, Float.NEGATIVE_INFINITY, Float.NEGATIVE_INFINITY);
        for (Spatial child : children) {
            MathUtil.updateMinMax(min, max, child.getWorldBounds().getMin());
            MathUtil.updateMinMax(min, max, child.getWorldBounds().getMax());
        }
        worldBounds.setMinMax(min, max);
    }

    @Override
    public void collide(Ray ray, ArrayList<CollisionResult> collisionResults) {
        for (Spatial child : children) {
            child.collide(ray, collisionResults);
        }
    }

    public void add(Spatial spatial) {
        spatial.setParent(this);
        children.add(spatial);
        modified = true;
    }

    public void removeAll() {
        for (Spatial spatial : children) {
            spatial.setParent(null);
        }
        children.clear();
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
