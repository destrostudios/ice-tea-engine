package com.destrostudios.icetea.core.model;

import com.destrostudios.icetea.core.clone.CloneContext;
import com.destrostudios.icetea.core.clone.ContextCloneable;
import com.destrostudios.icetea.core.data.UniformData;
import com.destrostudios.icetea.core.lifecycle.LifecycleObject;
import lombok.Getter;
import org.joml.Matrix4f;

public class Skeleton extends LifecycleObject implements ContextCloneable {

    public Skeleton(Joint[] joints) {
        this.joints = joints;
        jointMatrices = new Matrix4f[joints.length];
        for (int i = 0; i < jointMatrices.length; i++) {
            jointMatrices[i] = new Matrix4f();
        }
        uniformData = new UniformData();
    }

    public Skeleton(Skeleton skeleton, CloneContext context) {
        joints = new Joint[skeleton.joints.length];
        for (int i = 0; i < joints.length; i++) {
            joints[i] = context.cloneByReference(skeleton.joints[i]);
            // Set parent-child relationships afterwards to avoid circular cloning
            for (int r = 0; r < skeleton.joints[i].getChildren().length; r++) {
                joints[i].setChild(r, context.cloneByReference(skeleton.joints[i].getChildren()[r]));
            }
        }
        jointMatrices = new Matrix4f[skeleton.jointMatrices.length];
        for (int i = 0; i < jointMatrices.length; i++) {
            jointMatrices[i] = new Matrix4f(skeleton.jointMatrices[i]);
        }
        uniformData = skeleton.uniformData.clone(context);
    }
    private Joint[] joints;
    @Getter
    private Matrix4f[] jointMatrices;
    @Getter
    private UniformData uniformData;

    @Override
    protected void init() {
        super.init();
        updateUniformData();
    }

    @Override
    public void update(int imageIndex, float tpf) {
        super.update(imageIndex, tpf);
        boolean jointMatricesUpdated = false;
        for (Joint joint : joints) {
            joint.update();
        }
        // Check the joints after all were updated because a child might be in the array before its parent and would otherwise have a wrong world transform
        for (int i = 0; i < joints.length; i++) {
            Matrix4f jointMatrix = joints[i].getJointMatrix();
            if (!jointMatrices[i].equals(jointMatrix)) {
                jointMatrices[i].set(jointMatrix);
                jointMatricesUpdated = true;
            }
        }
        if (jointMatricesUpdated) {
            updateUniformData();
        }
        uniformData.update(application, imageIndex, tpf);
    }

    private void updateUniformData() {
        uniformData.setMatrix4fArray("jointMatrices", jointMatrices);
    }

    public void resetPose() {
        for (Joint joint : joints) {
            joint.resetPose();
        }
    }

    @Override
    protected void cleanupInternal() {
        uniformData.cleanup();
        super.cleanupInternal();
    }

    @Override
    public Skeleton clone(CloneContext context) {
        return new Skeleton(this, context);
    }
}
