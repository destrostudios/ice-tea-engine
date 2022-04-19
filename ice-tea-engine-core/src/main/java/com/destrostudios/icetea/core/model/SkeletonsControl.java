package com.destrostudios.icetea.core.model;

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
    public void update(int imageIndex, float tpf) {
        super.update(imageIndex, tpf);
        for (Skeleton skeleton : skeletons) {
            skeleton.update(application, imageIndex, tpf);
        }
    }

    @Override
    protected void cleanupInternal() {
        for (Skeleton skeleton : skeletons) {
            skeleton.cleanup();
        }
        super.cleanupInternal();
    }

    @Override
    public SkeletonsControl clone(CloneContext context) {
        return new SkeletonsControl(this, context);
    }
}
