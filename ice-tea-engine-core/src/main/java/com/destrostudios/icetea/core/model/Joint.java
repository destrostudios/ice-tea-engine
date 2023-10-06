package com.destrostudios.icetea.core.model;

import com.destrostudios.icetea.core.Transform;
import com.destrostudios.icetea.core.clone.CloneContext;
import com.destrostudios.icetea.core.clone.ContextCloneable;
import lombok.Getter;
import lombok.Setter;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public class Joint implements ContextCloneable {

    public Joint(int childrenCount, Transform localResetTransform) {
        children = new Joint[childrenCount];
        this.localResetTransform = localResetTransform;
        localPoseTransform = new Transform();
        worldPoseTransform = new Transform();
        jointMatrix = new Matrix4f();
        resetPose();
    }

    public Joint(Joint joint, CloneContext context) {
        // Set parent-child relationships afterwards to avoid circular cloning
        children = new Joint[joint.children.length];
        inverseBindMatrix = new Matrix4f(joint.inverseBindMatrix);
        localResetTransform = joint.localResetTransform.clone(context);
        localPoseTransform = joint.localPoseTransform.clone(context);
        worldPoseTransform = joint.worldPoseTransform.clone(context);
        jointMatrix = new Matrix4f(joint.jointMatrix);
    }
    @Getter
    private Joint parent;
    @Getter
    private Joint[] children;
    @Setter
    private Matrix4f inverseBindMatrix;
    private Transform localResetTransform;
    private Transform localPoseTransform;
    @Getter
    private Transform worldPoseTransform;
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

    public void updateJointMatrix(boolean wasParentWorldTransformUpdated) {
        boolean hasLocalTransformChanged = localPoseTransform.updateMatrixIfNecessary();
        boolean isWorldTransformOutdated = (hasLocalTransformChanged || wasParentWorldTransformUpdated);
        if (isWorldTransformOutdated) {
            updateWorldTransform();
        }
        for (Joint child : children) {
            child.updateJointMatrix(isWorldTransformOutdated);
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
    }

    @Override
    public Joint clone(CloneContext context) {
        return new Joint(this, context);
    }
}
