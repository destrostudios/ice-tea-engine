package com.destrostudios.icetea.core.model;

import com.destrostudios.icetea.core.Application;
import com.destrostudios.icetea.core.clone.CloneContext;
import com.destrostudios.icetea.core.scene.Control;
import lombok.Getter;

import java.util.Collection;
import java.util.LinkedList;

public class SkeletonsControl extends Control {

    public SkeletonsControl(SkeletonsControl skeletonsControl, CloneContext context) {
        skeletons = new LinkedList<>();
        for (Skeleton skeleton : skeletonsControl.skeletons) {
            skeletons.add(context.cloneByReference(skeleton));
        }
    }

    public SkeletonsControl(Collection<Skeleton> skeletons) {
        this.skeletons = skeletons;
    }
    @Getter
    private Collection<Skeleton> skeletons;

    @Override
    public void update(Application application, int imageIndex, float tpf) {
        super.update(application, imageIndex, tpf);
        for (Skeleton skeleton : skeletons) {
            skeleton.update(application, imageIndex, tpf);
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
    public SkeletonsControl clone(CloneContext context) {
        return new SkeletonsControl(this, context);
    }
}
