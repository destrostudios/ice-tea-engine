package com.destrostudios.icetea.core.compute;

import com.destrostudios.icetea.core.object.NativeObject;
import com.destrostudios.icetea.core.resource.ResourceDescriptorSet;
import lombok.Getter;

public abstract class ComputeAction extends NativeObject {

    @Getter
    protected ResourceDescriptorSet resourceDescriptorSet;

    @Override
    protected void initNative() {
        super.initNative();
        resourceDescriptorSet = new ResourceDescriptorSet();
        fillResourceDescriptorSet();
    }

    protected abstract void fillResourceDescriptorSet();
}
