package com.destrostudios.icetea.core.compute;

import com.destrostudios.icetea.core.buffer.PushConstantsDataBuffer;
import com.destrostudios.icetea.core.object.NativeObject;
import com.destrostudios.icetea.core.shader.Shader;
import lombok.Getter;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VkCommandBuffer;

import java.util.LinkedList;
import java.util.List;

import static org.lwjgl.vulkan.VK10.*;

public class ComputeActionGroup extends NativeObject {

    public ComputeActionGroup(Shader computeShader) {
        this.computeShader = computeShader;
        computePipeline = new ComputePipeline( this);
        computeActions = new LinkedList<>();
    }
    @Getter
    private Shader computeShader;
    private ComputePipeline computePipeline;
    @Getter
    protected List<ComputeAction> computeActions;

    protected int getPushConstantsSize() {
        return 0;
    }

    public void addComputeAction(ComputeAction computeAction) {
        computeActions.add(computeAction);
    }

    public void record(VkCommandBuffer commandBuffer, MemoryStack stack) {
        vkCmdBindPipeline(commandBuffer, VK_PIPELINE_BIND_POINT_COMPUTE, computePipeline.getPipeline());
    }

    protected void recordPushConstants(VkCommandBuffer commandBuffer, PushConstantsDataBuffer pushConstants) {
        vkCmdPushConstants(commandBuffer, computePipeline.getPipelineLayout(), VK_SHADER_STAGE_COMPUTE_BIT, 0, pushConstants.getBuffer().getByteBuffer());
    }

    protected void recordComputeAction(VkCommandBuffer commandBuffer, ComputeAction computeAction, MemoryStack stack) {
        vkCmdBindDescriptorSets(commandBuffer, VK_PIPELINE_BIND_POINT_COMPUTE, computePipeline.getPipelineLayout(), 0, computeAction.getResourceDescriptorSet().getDescriptorSets(0, 0, stack), null);
        vkCmdDispatch(commandBuffer, getGroupCountX(), getGroupCountY(), getGroupCountZ());
    }

    protected int getGroupCountX() {
        return 1;
    }

    protected int getGroupCountY() {
        return 1;
    }

    protected int getGroupCountZ() {
        return 1;
    }

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
