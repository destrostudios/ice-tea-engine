package com.destrostudios.icetea.core.compute;

import com.destrostudios.icetea.core.lifecycle.LifecycleObject;
import com.destrostudios.icetea.core.material.descriptor.MaterialDescriptorSet;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

public abstract class ComputeAction extends LifecycleObject {

    @Setter
    private ComputeActionGroup computeActionGroup;
    @Getter
    protected MaterialDescriptorSet materialDescriptorSet;
    private long descriptorPool;
    @Getter
    protected List<Long> descriptorSets;
    @Getter
    protected ComputePipeline computePipeline;

    @Override
    protected void init() {
        super.init();
        initMaterialDescriptorSet();
    }

    private void initMaterialDescriptorSet() {
        materialDescriptorSet = new MaterialDescriptorSet(application, computeActionGroup.getMaterialDescriptorSetLayout(), 1);
        fillMaterialDescriptorSet();
        descriptorPool = materialDescriptorSet.createDescriptorPool();
        descriptorSets = materialDescriptorSet.createDescriptorSets(descriptorPool);
    }

    protected abstract void fillMaterialDescriptorSet();

    @Override
    protected void cleanupInternal() {
        materialDescriptorSet.cleanupDescriptorSets(descriptorPool, descriptorSets);
        materialDescriptorSet.cleanupDescriptorPool(descriptorPool);
        super.cleanupInternal();
    }
}
