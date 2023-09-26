package com.destrostudios.icetea.core.compute;

import com.destrostudios.icetea.core.object.NativeObject;
import com.destrostudios.icetea.core.shader.Shader;
import lombok.Getter;
import org.lwjgl.vulkan.VkCommandBuffer;

import java.util.LinkedList;
import java.util.List;

import static org.lwjgl.vulkan.VK10.*;

public abstract class ComputeActionGroup extends NativeObject {

    public ComputeActionGroup() {
        computeActions = new LinkedList<>();
    }
    protected ComputePipeline computePipeline;
    @Getter
    protected List<ComputeAction> computeActions;

    @Override
    protected void initNative() {
        super.initNative();
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
    public void updateNative() {
        super.updateNative();
        for (ComputeAction computeAction : computeActions) {
            computeAction.updateNative(application);
        }
        computePipeline.updateNative(application);
    }

    @Override
    protected void cleanupNativeInternal() {
        computePipeline.cleanupNative();
        for (ComputeAction computeAction : computeActions) {
            computeAction.cleanupNative();
        }
        super.cleanupNativeInternal();
    }
}
