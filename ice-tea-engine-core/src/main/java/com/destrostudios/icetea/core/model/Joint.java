package com.destrostudios.icetea.core.model;

import com.destrostudios.icetea.core.Transform;
import lombok.Getter;
import lombok.Setter;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public class Joint {

    public Joint(int childrenCount, Transform localResetTransform) {
        children = new Joint[childrenCount];
        this.localResetTransform = localResetTransform;
        localPoseTransform = new Transform();
        worldPoseTransform = new Transform();
        isWorldTransformOutdated = true;
        jointMatrix = new Matrix4f();
        resetPose();
    }
    private Joint parent;
    private Joint[] children;
    @Setter
    private Matrix4f inverseBindMatrix;
    private Transform localResetTransform;
    private Transform localPoseTransform;
    @Getter
    private Transform worldPoseTransform;
    private boolean isWorldTransformOutdated;
    @Getter
    private Matrix4f jointMatrix;

    public void setChild(int index, Joint child) {
        children[index] = child;
        child.parent = this;
    }

    public void resetPose() {
        localPoseTransform.set(localResetTransform);
    }

    public void setLocalPoseTranslation(Vector3f translation) {
        localPoseTransform.setTranslation(translation);
    }

    public void setLocalPoseRotation(Quaternionf rotation) {
        localPoseTransform.setRotation(rotation);
    }

    public void setLocalPoseScale(Vector3f scale) {
        localPoseTransform.setScale(scale);
    }

    public void update() {
        if (localPoseTransform.updateMatrixIfNecessary()) {
            setWorldTransformOutdated();
        }
        if (isWorldTransformOutdated) {
            updateWorldTransform();
            // We have to make sure that the children are (at least once) updated after the parent, so they have the correct world transform
            for (Joint child : children) {
                child.update();
            }
        }
    }

    private void updateWorldTransform() {
        if (parent != null) {
            worldPoseTransform.setChildWorldTransform(parent.getWorldPoseTransform(), localPoseTransform);
            worldPoseTransform.updateMatrixIfNecessary();
        } else {
            worldPoseTransform.set(localPoseTransform);
        }
        worldPoseTransform.getMatrix().mul(inverseBindMatrix, jointMatrix);
        isWorldTransformOutdated = false;
    }

    private void setWorldTransformOutdated() {
        isWorldTransformOutdated = true;
        for (Joint child : children) {
            child.setWorldTransformOutdated();
        }
    }
}
