package com.destrostudios.icetea.core.compute;

import com.destrostudios.icetea.core.lifecycle.LifecycleObject;
import com.destrostudios.icetea.core.shader.Shader;
import lombok.Getter;
import org.lwjgl.vulkan.VkCommandBuffer;

import java.util.LinkedList;
import java.util.List;

import static org.lwjgl.vulkan.VK10.*;

public abstract class ComputeActionGroup extends LifecycleObject {

    public ComputeActionGroup() {
        computeActions = new LinkedList<>();
    }
    protected ComputePipeline computePipeline;
    @Getter
    protected List<ComputeAction> computeActions;

    @Override
    protected void init() {
        super.init();
        for (ComputeAction computeAction : computeActions) {
            computeAction.update(application, 0);
        }
        computePipeline = new ComputePipeline(this);
    }

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

    @Override
    protected void update(float tpf) {
        super.update(tpf);
        computePipeline.update(application, tpf);
    }

    @Override
    protected void cleanupInternal() {
        computePipeline.cleanup();
        for (ComputeAction computeAction : computeActions) {
            computeAction.cleanup();
        }
        super.cleanupInternal();
    }
}
