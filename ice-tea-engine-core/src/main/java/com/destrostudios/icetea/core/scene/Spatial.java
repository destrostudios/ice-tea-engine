package com.destrostudios.icetea.core.scene;

import com.destrostudios.icetea.core.Application;
import com.destrostudios.icetea.core.Transform;
import com.destrostudios.icetea.core.collision.BoundingBox;
import com.destrostudios.icetea.core.collision.CollisionResult;
import com.destrostudios.icetea.core.collision.Ray;
import com.destrostudios.icetea.core.light.Light;
import com.destrostudios.icetea.core.render.bucket.RenderBucketType;
import lombok.Getter;
import lombok.Setter;
import org.joml.*;

import java.util.*;

public abstract class Spatial {

    protected Spatial() {
        localTransform  = new Transform();
        worldTransform = new Transform();
        worldBounds = new BoundingBox();
        controls = new HashSet<>();
    }
    protected Application application;
    @Getter
    private Node parent;
    @Getter
    protected Transform localTransform;
    @Getter
    protected Transform worldTransform;
    @Getter
    protected BoundingBox worldBounds;
    private boolean isWorldTransformOutdated;
    private boolean isWorldBoundsOutdated;
    @Getter
    private Set<Control> controls;
    @Setter
    @Getter
    private RenderBucketType renderBucket;

    public boolean update(Application application, float tpf) {
        if (this.application == null) {
            this.application = application;
            init();
        }
        ensureControlsState();
        for (Control control : controls) {
            control.update(tpf);
        }
        if (localTransform.updateMatrixIfNecessary()) {
            setWorldTransformOutdated();
            setWorldBoundsOutdated();
        }
        updateWorldTransformIfNecessary();
        return false;
    }

    protected void init() {
        setWorldTransformOutdated();
        setWorldBoundsOutdated();
        ensureControlsState();
    }

    protected void setWorldTransformOutdated() {
        isWorldTransformOutdated = true;
    }

    protected void setWorldBoundsOutdated() {
        isWorldBoundsOutdated = true;
        if (parent != null) {
            parent.setWorldBoundsOutdated();
        }
    }

    private void ensureControlsState() {
        for (Control control : controls) {
            if (!control.isInitialized()) {
                control.init(application);
            }
            control.setSpatial(this);
        }
    }

    protected void updateWorldTransformIfNecessary() {
        if (isWorldTransformOutdated) {
            updateWorldTransform();
            isWorldTransformOutdated = false;
        }
    }

    protected void updateWorldTransform() {
        if (parent != null) {
            worldTransform.setChildWorldTransform(parent.getWorldTransform(), localTransform);
            worldTransform.updateMatrixIfNecessary();
        } else {
            worldTransform.set(localTransform);
        }
    }

    protected void updateWorldBoundsIfNecessary() {
        if (isWorldBoundsOutdated) {
            updateWorldBounds();
            isWorldBoundsOutdated = false;
        }
    }

    protected abstract void updateWorldBounds();

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

    public List<Light> getAffectingLights() {
        List<Light> affectingLights = new LinkedList<>();
        Light light = application.getLight();
        if ((light != null) && light.isAffecting(this)) {
            affectingLights.add(light);
        }
        return affectingLights;
    }

    public void updateUniformBuffers(int currentImage) {
        for (Control control : controls) {
            control.updateUniformBuffers(currentImage);
        }
    }

    public abstract void collide(Ray ray, ArrayList<CollisionResult> collisionResults);

    public void addControl(Control control) {
        controls.add(control);
    }

    public void removeControl(Control control) {
        if (controls.remove(control)) {
            if (isAttachedToRoot()) {
                control.onRemoveFromRoot();
            }
            control.onRemove();
        }
    }

    public void setParent(Node parent) {
        if ((parent == null) && isAttachedToRoot()) {
            onRemoveFromRoot();
        }
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

    private void onRemoveFromRoot() {
        for (Control control : controls) {
            control.onRemoveFromRoot();
        }
    }
}
