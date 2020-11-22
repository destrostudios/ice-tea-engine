package com.destrostudios.icetea.core;

import lombok.Getter;
import lombok.Setter;
import org.joml.*;

import java.util.LinkedList;
import java.util.List;

public class Spatial {

    protected Application application;
    @Getter
    @Setter
    private Node parent;
    @Getter
    protected Transform localTransform = new Transform();
    @Getter
    protected Transform worldTransform = new Transform();
    @Setter
    private boolean isWorldTransformOutdated;

    public boolean update(Application application) {
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
        return false;
    }

    protected void init() {
        isWorldTransformOutdated = true;
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
}
