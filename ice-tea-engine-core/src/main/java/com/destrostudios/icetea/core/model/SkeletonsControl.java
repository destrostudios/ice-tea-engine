package com.destrostudios.icetea.core.model;

import com.destrostudios.icetea.core.Application;
import com.destrostudios.icetea.core.clone.CloneContext;
import com.destrostudios.icetea.core.scene.Control;
import lombok.Getter;

import java.util.Collection;
import java.util.LinkedList;

public class SkeletonsControl extends Control {

    public SkeletonsControl(Collection<Skeleton> skeletons) {
        this.skeletons = skeletons;
    }

    public SkeletonsControl(SkeletonsControl skeletonsControl, CloneContext context) {
        skeletons = new LinkedList<>();
        for (Skeleton skeleton : skeletonsControl.skeletons) {
            skeletons.add(context.cloneByReference(skeleton));
        }
    }
    @Getter
    private Collection<Skeleton> skeletons;

    @Override
    public void updateLogicalState(Application application, float tpf) {
        super.updateLogicalState(application, tpf);
        for (Skeleton skeleton : skeletons) {
            skeleton.updateLogicalState(application, tpf);
        }
    }

    @Override
    public void applyLogicalState() {
        super.applyLogicalState();
        for (Skeleton skeleton : skeletons) {
            skeleton.applyLogicalState();
        }
    }

    @Override
    public void updateNativeState(Application application) {
        super.updateNativeState(application);
        for (Skeleton skeleton : skeletons) {
            skeleton.updateNativeState(application);
        }
    }

    @Override
    public void cleanupNativeStateInternal() {
        for (Skeleton skeleton : skeletons) {
            skeleton.cleanupNativeState();
        }
        super.cleanupNativeStateInternal();
    }

    @Override
    public SkeletonsControl clone(CloneContext context) {
        return new SkeletonsControl(this, context);
    }
}
