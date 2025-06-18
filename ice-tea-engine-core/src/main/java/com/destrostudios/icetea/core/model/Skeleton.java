package com.destrostudios.icetea.core.model;

import com.destrostudios.icetea.core.Application;
import com.destrostudios.icetea.core.clone.CloneContext;
import com.destrostudios.icetea.core.clone.ContextCloneable;
import com.destrostudios.icetea.core.buffer.UniformDataBuffer;
import com.destrostudios.icetea.core.object.LogicalObject;
import com.destrostudios.icetea.core.resource.descriptor.SkeletonDescriptor;
import lombok.Getter;
import org.joml.Matrix4f;

public class Skeleton extends LogicalObject implements ContextCloneable {

    public Skeleton(SkeletonNode[] nodes, Joint[] joints) {
        this.nodes = nodes;
        this.joints = joints;
        jointMatrices = new Matrix4f[joints.length];
        for (int i = 0; i < jointMatrices.length; i++) {
            jointMatrices[i] = new Matrix4f();
        }
        uniformBuffer = new UniformDataBuffer();
        uniformBuffer.setDescriptor("default", new SkeletonDescriptor());
    }

    public Skeleton(Skeleton skeleton, CloneContext context) {
        nodes = new SkeletonNode[skeleton.nodes.length];
        joints = new Joint[skeleton.joints.length];
        for (int i = 0; i < nodes.length; i++) {
            nodes[i] = context.cloneByReference(skeleton.nodes[i]);
            // Set parent-child relationships afterwards to avoid circular cloning
            for (int r = 0; r < skeleton.nodes[i].getChildren().length; r++) {
                nodes[i].setChild(r, context.cloneByReference(skeleton.nodes[i].getChildren()[r]));
            }
            for (int r = 0; r < joints.length; r++) {
                if (skeleton.joints[r] == skeleton.nodes[i]) {
                    joints[r] = (Joint) nodes[i];
                    break;
                }
            }
        }
        jointMatrices = new Matrix4f[skeleton.jointMatrices.length];
        for (int i = 0; i < jointMatrices.length; i++) {
            jointMatrices[i] = new Matrix4f(skeleton.jointMatrices[i]);
        }
        uniformBuffer = skeleton.uniformBuffer.clone(context);
    }
    private SkeletonNode[] nodes;
    private Joint[] joints;
    @Getter
    private Matrix4f[] jointMatrices;
    @Getter
    private UniformDataBuffer uniformBuffer;

    @Override
    public void applyLogicalState() {
        super.applyLogicalState();
        for (SkeletonNode node : nodes) {
            if (node.getParent() == null) {
                node.updateWorldTransform();
            }
        }
        for (int i = 0; i < joints.length; i++) {
            jointMatrices[i].set(joints[i].getJointMatrix());
        }
        uniformBuffer.getData().setMatrix4fArray("jointMatrices", jointMatrices);
    }

    @Override
    public void updateNativeState(Application application) {
        super.updateNativeState(application);
        uniformBuffer.updateNative(application);
    }

    public void resetLocalTransform() {
        for (SkeletonNode node : nodes) {
            node.resetLocalTransform();
        }
    }

    @Override
    public void cleanupNativeStateInternal() {
        uniformBuffer.cleanupNative();
        super.cleanupNativeStateInternal();
    }

    @Override
    public Skeleton clone(CloneContext context) {
        return new Skeleton(this, context);
    }
}
