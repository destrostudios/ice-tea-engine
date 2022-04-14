package com.destrostudios.icetea.core.compute;

import com.destrostudios.icetea.core.Application;
import com.destrostudios.icetea.core.material.descriptor.MaterialDescriptorSetLayout;
import com.destrostudios.icetea.core.shader.Shader;
import lombok.Getter;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VkCommandBuffer;

import java.util.LinkedList;
import java.util.List;

import static org.lwjgl.vulkan.VK10.*;

public abstract class ComputeActionGroup {

    public ComputeActionGroup() {
        computeActions = new LinkedList<>();
    }
    private Application application;
    @Getter
    protected MaterialDescriptorSetLayout materialDescriptorSetLayout;
    protected ComputePipeline computePipeline;
    @Getter
    protected List<ComputeAction> computeActions;

    public void init(Application application) {
        this.application = application;
        initMaterialDescriptorSetLayout();
        for (ComputeAction computeAction : computeActions) {
            computeAction.init(application, this);
        }
        computePipeline = new ComputePipeline(application, this);
        computePipeline.init();
    }

    private void initMaterialDescriptorSetLayout() {
        materialDescriptorSetLayout = new MaterialDescriptorSetLayout(application);
        fillMaterialDescriptorLayout();
        materialDescriptorSetLayout.initDescriptorSetLayout();
    }

    protected abstract void fillMaterialDescriptorLayout();

    public abstract Shader getComputeShader();

    protected int getPushConstantsSize() {
        return 0;
    }

    public void addComputeAction(ComputeAction computeAction) {
        computeActions.add(computeAction);
    }

    public void record(VkCommandBuffer commandBuffer) {
        vkCmdBindPipeline(commandBuffer, VK_PIPELINE_BIND_POINT_COMPUTE, computePipeline.getPipeline());
    }

    protected abstract int getGroupCountX();

    protected abstract int getGroupCountY();

    protected abstract int getGroupCountZ();

    public void cleanup() {
        computePipeline.cleanup();
        for (ComputeAction computeAction : computeActions) {
            computeAction.cleanup();
        }
        materialDescriptorSetLayout.cleanupDescriptorSetLayout();
    }
}
