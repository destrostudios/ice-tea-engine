package com.destrostudios.icetea.core.compute;

import com.destrostudios.icetea.core.Application;
import com.destrostudios.icetea.core.material.descriptor.MaterialDescriptorSet;
import lombok.Getter;

import java.util.List;

public abstract class ComputeAction {

    private Application application;
    private ComputeActionGroup computeActionGroup;
    @Getter
    protected MaterialDescriptorSet materialDescriptorSet;
    private long descriptorPool;
    @Getter
    protected List<Long> descriptorSets;
    @Getter
    protected ComputePipeline computePipeline;

    public void init(Application application, ComputeActionGroup computeActionGroup) {
        this.application = application;
        this.computeActionGroup = computeActionGroup;
        initMaterialDescriptorSet();
    }

    private void initMaterialDescriptorSet() {
        materialDescriptorSet = new MaterialDescriptorSet(application, computeActionGroup.getMaterialDescriptorSetLayout(), 1);
        fillMaterialDescriptorSet();
        descriptorPool = materialDescriptorSet.createDescriptorPool();
        descriptorSets = materialDescriptorSet.createDescriptorSets(descriptorPool);
    }

    protected abstract void fillMaterialDescriptorSet();

    public void cleanup() {
        materialDescriptorSet.cleanupDescriptorSets(descriptorPool, descriptorSets);
        materialDescriptorSet.cleanupDescriptorPool(descriptorPool);
    }
}
