package com.destrostudios.icetea.core.model;

import com.destrostudios.icetea.core.Transform;
import com.destrostudios.icetea.core.clone.CloneContext;
import com.destrostudios.icetea.core.clone.ContextCloneable;
import lombok.Getter;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public class SkeletonNode implements ContextCloneable {

    public SkeletonNode(int childrenCount, Transform localResetTransform) {
        children = new SkeletonNode[childrenCount];
        this.localResetTransform = localResetTransform;
        localTransform = new Transform();
        worldTransform = new Transform();
        resetLocalTransform();
    }

    public SkeletonNode(SkeletonNode skeletonNode, CloneContext context) {
        // Set parent-child relationships afterwards to avoid circular cloning
        children = new SkeletonNode[skeletonNode.children.length];
        localResetTransform = skeletonNode.localResetTransform.clone(context);
        localTransform = skeletonNode.localTransform.clone(context);
        worldTransform = skeletonNode.worldTransform.clone(context);
    }
    @Getter
    private SkeletonNode parent;
    @Getter
    private SkeletonNode[] children;
    private Transform localResetTransform;
    protected Transform localTransform;
    protected Transform worldTransform;

    public void setChild(int index, SkeletonNode child) {
        children[index] = child;
        child.parent = this;
    }

    public void resetLocalTransform() {
        localTransform.set(localResetTransform);
    }

    public void setLocalTranslation(Vector3f translation) {
        localTransform.setTranslation(translation);
    }

    public void setLocalRotation(Quaternionf rotation) {
        localTransform.setRotation(rotation);
    }

    public void setLocalScale(Vector3f scale) {
        localTransform.setScale(scale);
    }

    public void updateWorldTransform() {
        if (localTransform.updateMatrixIfNecessary()) {
            if (parent != null) {
                worldTransform.setMultiplicationResult(parent.worldTransform, localTransform);
                worldTransform.updateMatrixIfNecessary();
            } else {
                worldTransform.set(localTransform);
            }
            onWorldTransformUpdated();
        }
        for (SkeletonNode child : children) {
            child.updateWorldTransform();
        }
    }

    protected void onWorldTransformUpdated() {

    }

    @Override
    public SkeletonNode clone(CloneContext context) {
        return new SkeletonNode(this, context);
    }
}
