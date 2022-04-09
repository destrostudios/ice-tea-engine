package com.destrostudios.icetea.core.scene;

import com.destrostudios.icetea.core.Application;
import com.destrostudios.icetea.core.clone.CloneContext;
import com.destrostudios.icetea.core.collision.BoundingBox;
import com.destrostudios.icetea.core.collision.CollisionResult;
import com.destrostudios.icetea.core.collision.Ray;
import com.destrostudios.icetea.core.util.MathUtil;
import lombok.Getter;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class Node extends Spatial {

    public Node() { }

    public Node(Node node, CloneContext context) {
        super(node, context);
        for (Spatial child : node.children) {
            // Set parent-child relationships afterwards to avoid circular cloning
            add(context.cloneByReference(child));
        }
    }
    @Getter
    private List<Spatial> children = new LinkedList<>();
    private boolean childrenModified;

    @Override
    public boolean update(Application application, float tpf) {
        boolean commandBufferOutdated = super.update(application, tpf);
        for (Spatial child : children) {
            commandBufferOutdated |= child.update(application, tpf);
        }
        updateWorldBoundsIfNecessary();
        updateShadowReceiveWorldBoundsIfNecessary();
        if (childrenModified) {
            commandBufferOutdated = true;
            childrenModified = false;
        }
        return commandBufferOutdated;
    }

    @Override
    protected void setWorldTransformOutdated() {
        super.setWorldTransformOutdated();
        for (Spatial child : children) {
            child.setWorldTransformOutdated();
            child.setWorldBoundsOutdated();
        }
    }

    @Override
    protected void updateWorldBounds(BoundingBox destinationWorldBounds, Predicate<Spatial> isSpatialConsidered) {
        // TODO: Introduce TempVars
        Vector3f min = new Vector3f();
        Vector3f max = new Vector3f();
        boolean wasChildConsidered = false;
        for (Spatial child : children) {
            if (isSpatialConsidered.test(child)) {
                if (!wasChildConsidered) {
                    min.set(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY);
                    max.set(Float.NEGATIVE_INFINITY, Float.NEGATIVE_INFINITY, Float.NEGATIVE_INFINITY);
                }
                wasChildConsidered = true;
                MathUtil.updateMinMax(min, max, child.getWorldBounds().getMin());
                MathUtil.updateMinMax(min, max, child.getWorldBounds().getMax());
            }
        }
        destinationWorldBounds.setMinMax(min, max);
    }

    @Override
    public void updateUniformBuffers(int currentImage) {
        super.updateUniformBuffers(currentImage);
        for (Spatial child : children) {
            child.updateUniformBuffers(currentImage);
        }
    }

    @Override
    protected void collideStatic(Ray ray, Matrix4f worldMatrix, float worldBoundsTMin, float worldBoundsTMax, ArrayList<CollisionResult> collisionResults) {
        for (Spatial child : children) {
            child.collideStatic(ray, collisionResults);
        }
    }

    @Override
    public void collideDynamic(Ray ray, ArrayList<CollisionResult> collisionResults) {
        for (Spatial child : children) {
            child.collideDynamic(ray, collisionResults);
        }
    }

    public void add(Spatial spatial) {
        spatial.setParent(this);
        children.add(spatial);
        childrenModified = true;
    }

    public void removeAll() {
        for (Spatial spatial : children) {
            spatial.setParent(null);
        }
        children.clear();
        childrenModified = true;
    }

    public void remove(Spatial spatial) {
        spatial.setParent(null);
        children.remove(spatial);
        childrenModified = true;
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

    @Override
    public Node clone(CloneContext context) {
        return new Node(this, context);
    }
}
