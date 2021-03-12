package com.destrostudios.icetea.core.scene;

import com.destrostudios.icetea.core.Application;
import com.destrostudios.icetea.core.Transform;
import com.destrostudios.icetea.core.light.Light;
import com.destrostudios.icetea.core.render.bucket.RenderBucketType;
import lombok.Getter;
import lombok.Setter;
import org.joml.*;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class Spatial {

    protected Spatial() {
        localTransform  = new Transform();
        worldTransform = new Transform();
        controls = new HashSet<>();
    }
    protected Application application;
    @Getter
    private Node parent;
    @Getter
    protected Transform localTransform;
    @Getter
    protected Transform worldTransform;
    @Setter
    private boolean isWorldTransformOutdated;
    private Set<Control> controls;
    @Setter
    @Getter
    private RenderBucketType renderBucket;

    public boolean update(Application application, float tpf) {
        if (this.application == null) {
            this.application = application;
            init();
        }
        if (localTransform.updateMatrixIfNecessary()) {
            isWorldTransformOutdated = true;
        }
        if (isWorldTransformOutdated) {
            updateWorldTransform();
            isWorldTransformOutdated = false;
        }
        ensureControlsState();
        for (Control control : controls) {
            control.update(tpf);
        }
        return false;
    }

    protected void init() {
        isWorldTransformOutdated = true;
        ensureControlsState();
    }

    private void ensureControlsState() {
        for (Control control : controls) {
            if (!control.isInitialized()) {
                control.init(application);
            }
            control.setSpatial(this);
        }
    }

    protected void updateWorldTransform() {
        if (parent != null) {
            Transform parentWorldTransform = parent.getWorldTransform();
            Vector3f worldTranslation = parentWorldTransform.getTranslation().add(parentWorldTransform.getRotation().transform(localTransform.getTranslation(), new Vector3f()), new Vector3f());
            Quaternionf worldRotation = parentWorldTransform.getRotation().mul(localTransform.getRotation(), new Quaternionf());
            Vector3fc worldScale = parentWorldTransform.getScale().mul(localTransform.getScale(), new Vector3f());
            worldTransform.setTranslation(worldTranslation);
            worldTransform.setRotation(worldRotation);
            worldTransform.setScale(worldScale);
            worldTransform.updateMatrixIfNecessary();
        } else {
            worldTransform.set(localTransform);
        }
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

    protected void onWorldTransformOutdated() {
        isWorldTransformOutdated = true;
    }

    public List<Light> getAffectingLights() {
        List<Light> affectingLights = new LinkedList<>();
        Light light = application.getLight();
        if ((light != null) && light.isAffecting(this)) {
            affectingLights.add(light);
        }
        return affectingLights;
    }

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
