package com.destrostudios.icetea.core.model;

import com.destrostudios.icetea.core.Application;
import com.destrostudios.icetea.core.data.UniformData;
import lombok.Getter;
import org.joml.Matrix4f;

public class Skeleton {

    public Skeleton(Joint[] joints) {
        this.joints = joints;
        jointMatrices = new Matrix4f[joints.length];
        for (int i = 0; i < jointMatrices.length; i++) {
            jointMatrices[i] = new Matrix4f();
        }
        uniformData = new UniformData();
    }
    private Joint[] joints;
    @Getter
    private Matrix4f[] jointMatrices;
    @Getter
    private UniformData uniformData;

    public void init(Application application) {
        uniformData.setApplication(application);
        updateUniformData();
        uniformData.initBuffers(application.getSwapChain().getImages().size());
    }

    public void resetPose() {
        for (Joint joint : joints) {
            joint.resetPose();
        }
    }

    public void update() {
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
    }

    private void updateUniformData() {
        uniformData.setMatrix4fArray("jointMatrices", jointMatrices);
    }

    public void cleanup() {
        uniformData.cleanupBuffer();
    }
}
