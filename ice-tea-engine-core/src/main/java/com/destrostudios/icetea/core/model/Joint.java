package com.destrostudios.icetea.core.model;

import com.destrostudios.icetea.core.Transform;
import com.destrostudios.icetea.core.clone.CloneContext;
import com.destrostudios.icetea.core.clone.ContextCloneable;
import lombok.Getter;
import lombok.Setter;
import org.joml.Matrix4f;

public class Joint extends SkeletonNode implements ContextCloneable {

    public Joint(int childrenCount, Transform localTransform) {
        super(childrenCount, localTransform);
        jointMatrix = new Matrix4f();
    }

    public Joint(Joint joint, CloneContext context) {
        super(joint, context);
        inverseBindMatrix = new Matrix4f(joint.inverseBindMatrix);
        jointMatrix = new Matrix4f(joint.jointMatrix);
    }
    @Setter
    private Matrix4f inverseBindMatrix;
    @Getter
    private Matrix4f jointMatrix;

    @Override
    protected void onWorldTransformUpdated() {
        super.onWorldTransformUpdated();
        worldTransform.getMatrix().mul(inverseBindMatrix, jointMatrix);
    }

    @Override
    public Joint clone(CloneContext context) {
        return new Joint(this, context);
    }
}
