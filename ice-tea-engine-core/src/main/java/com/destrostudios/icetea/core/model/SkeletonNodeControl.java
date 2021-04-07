package com.destrostudios.icetea.core.model;

import com.destrostudios.icetea.core.Application;
import com.destrostudios.icetea.core.scene.Control;
import lombok.Getter;

public class SkeletonNodeControl extends Control {

    public SkeletonNodeControl(Skeleton skeleton) {
        this.skeleton = skeleton;
    }
    @Getter
    private Skeleton skeleton;

    @Override
    public void init(Application application) {
        super.init(application);
        skeleton.init(application);
    }

    @Override
    public void update(float tpf) {
        super.update(tpf);
        skeleton.update();
    }

    @Override
    public void updateUniformBuffers(int currentImage) {
        super.updateUniformBuffers(currentImage);
        skeleton.getUniformData().updateBufferIfNecessary(currentImage);
    }

    @Override
    public void cleanup() {
        super.cleanup();
        skeleton.cleanup();
    }
}
