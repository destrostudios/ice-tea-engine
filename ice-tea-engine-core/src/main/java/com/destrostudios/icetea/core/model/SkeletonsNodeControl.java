package com.destrostudios.icetea.core.model;

import com.destrostudios.icetea.core.Application;
import com.destrostudios.icetea.core.clone.CloneContext;
import com.destrostudios.icetea.core.scene.Control;
import lombok.Getter;

import java.util.Collection;
import java.util.LinkedList;

public class SkeletonsNodeControl extends Control {

    public SkeletonsNodeControl(SkeletonsNodeControl skeletonsNodeControl, CloneContext context) {
        skeletons = new LinkedList<>();
        for (Skeleton skeleton : skeletonsNodeControl.skeletons) {
            skeletons.add(context.cloneByReference(skeleton));
        }
    }

    public SkeletonsNodeControl(Collection<Skeleton> skeletons) {
        this.skeletons = skeletons;
    }
    @Getter
    private Collection<Skeleton> skeletons;

    @Override
    public void init(Application application) {
        super.init(application);
        for (Skeleton skeleton : skeletons) {
            skeleton.init(application);
        }
    }

    @Override
    public void update(float tpf) {
        super.update(tpf);
        for (Skeleton skeleton : skeletons) {
            skeleton.update();
        }
    }

    @Override
    public void updateUniformBuffers(int currentImage) {
        super.updateUniformBuffers(currentImage);
        for (Skeleton skeleton : skeletons) {
            skeleton.getUniformData().updateBufferIfNecessary(currentImage);
        }
    }

    @Override
    public void cleanup() {
        super.cleanup();
        for (Skeleton skeleton : skeletons) {
            skeleton.cleanup();
        }
    }

    @Override
    public SkeletonsNodeControl clone(CloneContext context) {
        return new SkeletonsNodeControl(this, context);
    }
}
