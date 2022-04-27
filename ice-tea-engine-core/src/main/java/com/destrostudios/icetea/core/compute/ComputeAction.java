package com.destrostudios.icetea.core.compute;

import com.destrostudios.icetea.core.lifecycle.LifecycleObject;
import com.destrostudios.icetea.core.resource.ResourceDescriptorSet;
import lombok.Getter;

public abstract class ComputeAction extends LifecycleObject {

    @Getter
    protected ResourceDescriptorSet resourceDescriptorSet;

    @Override
    protected void init() {
        super.init();
        resourceDescriptorSet = new ResourceDescriptorSet();
        fillResourceDescriptorSet();
    }

    protected abstract void fillResourceDescriptorSet();
}
