package com.destrostudios.icetea.core.scene;

import com.destrostudios.icetea.core.Application;
import com.destrostudios.icetea.core.Transform;
import com.destrostudios.icetea.core.clone.CloneContext;
import com.destrostudios.icetea.core.clone.ContextCloneable;
import com.destrostudios.icetea.core.collision.BoundingBox;
import com.destrostudios.icetea.core.collision.CollisionResult;
import com.destrostudios.icetea.core.collision.CollisionResult_AABB_Ray;
import com.destrostudios.icetea.core.collision.Ray;
import com.destrostudios.icetea.core.object.LogicalObject;
import com.destrostudios.icetea.core.resource.AdditionalResourceDescriptorProvider;
import com.destrostudios.icetea.core.resource.ResourceDescriptor;
import com.destrostudios.icetea.core.mesh.VertexPositionModifier;
import com.destrostudios.icetea.core.render.bucket.RenderBucketType;
import com.destrostudios.icetea.core.render.shadow.ShadowMode;
import lombok.Getter;
import lombok.Setter;
import org.joml.*;

import java.util.*;
import java.util.function.Predicate;

public abstract class Spatial extends LogicalObject implements ContextCloneable {

    protected Spatial() {
        localTransform = new Transform();
        worldTransform = new Transform();
        worldBounds = new BoundingBox();
        shadowMode = ShadowMode.INHERIT;
        worldBoundsShadowReceive = new BoundingBox();
    }

    protected Spatial(Spatial spatial, CloneContext context) {
        name = spatial.name;
        // Set parent-child relationships afterwards to avoid circular cloning
        localTransform = spatial.localTransform.clone(context);
        worldTransform = spatial.worldTransform.clone(context);
        worldBounds = spatial.worldBounds.clone(context);
        shadowMode = spatial.shadowMode;
        worldBoundsShadowReceive = spatial.worldBoundsShadowReceive.clone(context);
        for (Control control : spatial.controls) {
            controls.add(control.clone(context));
        }
        renderBucket = spatial.renderBucket;
    }
    @Getter
    @Setter
    private String name;
    @Getter
    private Node parent;
    @Getter
    protected Transform localTransform;
    @Getter
    protected Transform worldTransform;
    @Getter
    protected BoundingBox worldBounds;
    @Getter
    private BoundingBox worldBoundsShadowReceive;
    @Getter
    protected Set<Control> controls = new HashSet<>();
    @Getter
    @Setter
    private RenderBucketType renderBucket;
    @Setter
    private boolean affectedByLight;
    @Getter
    @Setter
    private ShadowMode shadowMode;
    // TODO: Introduce TempVars
    private LinkedList<ResourceDescriptor> tmpAdditionalResourceDescriptors = new LinkedList<>();
    private LinkedList<VertexPositionModifier> tmpVertexPositionModifiers = new LinkedList<>();

    @Override
    public void updateLogicalState(Application application, float tpf) {
        super.updateLogicalState(application, tpf);
        for (Control control : controls) {
            control.updateLogicalState(application, tpf);
        }
    }

    @Override
    public void applyLogicalState() {
        super.applyLogicalState();
        for (Control control : controls) {
            control.applyLogicalState();
        }
        updateTransform();
    }

    @Override
    public void updateNativeState(Application application) {
        super.updateNativeState(application);
        for (Control control : controls) {
            control.updateNativeState(application);
        }
    }

    public void updateTransform() {
        localTransform.updateMatrixIfNecessary();
        if (parent != null) {
            worldTransform.combineWithParent(parent.getWorldTransform(), localTransform);
            worldTransform.updateMatrixIfNecessary();
        } else {
            worldTransform.set(localTransform);
        }
    }

    protected abstract void updateWorldBounds(BoundingBox destinationWorldBounds, Predicate<Spatial> isSpatialConsidered);

    protected void updateWorldBounds() {
        updateWorldBounds(worldBounds, spatial -> true);
        updateWorldBounds(worldBoundsShadowReceive, Spatial::isReceivingShadows);
    }

    public void setLocalTransform(Transform transform) {
        localTransform.set(transform);
    }

    public void setLocalTransform(Matrix4fc transform) {
        localTransform.set(transform);
    }

    public void setLocalTranslation(Vector3fc translation) {
        localTransform.setTranslation(translation);
    }

    public void setLocalRotation(Quaternionfc rotation) {
        localTransform.setRotation(rotation);
    }

    public void setLocalScale(Vector3fc scale) {
        localTransform.setScale(scale);
    }

    public void move(Vector3fc translation) {
        localTransform.move(translation);
    }

    public void rotate(Quaternionfc rotation) {
        localTransform.rotate(rotation);
    }

    public void scale(Vector3fc scale) {
        localTransform.scale(scale);
    }

    public void collideStatic(Ray ray, ArrayList<CollisionResult> collisionResults) {
        CollisionResult_AABB_Ray worldBoundCollision = worldBounds.collide(ray);
        if (worldBoundCollision != null) {
           collideStatic(ray, worldTransform.getMatrix(), worldBoundCollision.getTMin(), worldBoundCollision.getTMax(), collisionResults);
        }
    }

    protected abstract void collideStatic(Ray ray, Matrix4f worldMatrix, float worldBoundsTMin, float worldBoundsTMax, ArrayList<CollisionResult> collisionResults);

    public abstract void collideDynamic(Ray ray, ArrayList<CollisionResult> collisionResults);

    public boolean isAffectedByLight() {
       return (affectedByLight || ((parent != null) && parent.isAffectedByLight()));
    }

    public boolean isReceivingShadows() {
        return (shadowMode == ShadowMode.RECEIVE)
            || (shadowMode == ShadowMode.CAST_AND_RECEIVE)
            || ((shadowMode == ShadowMode.INHERIT) && ((parent != null) && parent.isReceivingShadows()));
    }

    public boolean isCastingShadows() {
        return (shadowMode == ShadowMode.CAST)
            || (shadowMode == ShadowMode.CAST_AND_RECEIVE)
            || ((shadowMode == ShadowMode.INHERIT) && ((parent != null) && parent.isCastingShadows()));
    }

    public void addControl(Control control) {
        control.setSpatial(this);
        controls.add(control);
    }

    public void removeControl(Control control) {
        if (controls.remove(control)) {
            control.setSpatial(null);
        }
    }

    public <T extends Control> T getFirstControl(Class<T> controlClass) {
        for (Control control : controls) {
            if (controlClass.isAssignableFrom(control.getClass())) {
                return (T) control;
            }
        }
        return null;
    }

    public void removeFromParent() {
        if (parent != null) {
            parent.remove(this);
        }
    }

    protected void setParent(Node parent) {
        this.parent = parent;
    }

    public boolean isAttachedToRoot() {
        return (application != null) && hasParent(application.getRootNode());
    }

    public boolean hasParent(Spatial spatial) {
        Spatial currentSpatial = parent;
        while (currentSpatial != null) {
            if (currentSpatial == spatial) {
                return true;
            }
            currentSpatial = currentSpatial.getParent();
        }
        return false;
    }

    protected void addAdditionalResourceDescriptors(Geometry geometry, Map<String, ResourceDescriptor<?>> resourceDescriptors) {
        tmpAdditionalResourceDescriptors.clear();
        if (parent != null) {
            parent.addAdditionalResourceDescriptors(geometry, resourceDescriptors);
        }
        for (Control control : controls) {
            if (control instanceof AdditionalResourceDescriptorProvider) {
                AdditionalResourceDescriptorProvider additionalResourceDescriptorProvider = (AdditionalResourceDescriptorProvider) control;
                additionalResourceDescriptorProvider.addAdditionalResourceDescriptors(geometry, resourceDescriptors);
            }
        }
    }

    protected List<VertexPositionModifier> getVertexPositionModifiers() {
        tmpVertexPositionModifiers.clear();
        if (parent != null) {
            tmpVertexPositionModifiers.addAll(parent.getVertexPositionModifiers());
        }
        for (Control control : controls) {
            if (control instanceof VertexPositionModifier) {
                VertexPositionModifier vertexPositionModifier = (VertexPositionModifier) control;
                tmpVertexPositionModifiers.add(vertexPositionModifier);
            }
        }
        return tmpVertexPositionModifiers;
    }

    @Override
    public void cleanupNativeStateInternal() {
        for (Control control : controls) {
            control.cleanupNativeState();
        }
        super.cleanupNativeStateInternal();
    }

    @Override
    public abstract Spatial clone(CloneContext context);
}
