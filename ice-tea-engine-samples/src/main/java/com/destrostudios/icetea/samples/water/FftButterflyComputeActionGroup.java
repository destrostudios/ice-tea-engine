package com.destrostudios.icetea.samples.water;

import com.destrostudios.icetea.core.compute.ComputeAction;
import com.destrostudios.icetea.core.compute.ComputeActionGroup;
import com.destrostudios.icetea.core.buffer.ByteDataBuffer;
import com.destrostudios.icetea.core.shader.Shader;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VkCommandBuffer;
import org.lwjgl.vulkan.VkMemoryBarrier;

import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.vulkan.VK10.*;

public class FftButterflyComputeActionGroup extends ComputeActionGroup {

    public FftButterflyComputeActionGroup(int n, ByteDataBuffer[] horizontalPushConstants, ByteDataBuffer[] verticalPushConstants) {
        this.n = n;
        this.horizontalPushConstants = horizontalPushConstants;
        this.verticalPushConstants = verticalPushConstants;
    }
    private int n;
    private ByteDataBuffer[] horizontalPushConstants;
    private ByteDataBuffer[] verticalPushConstants;

    @Override
    public Shader getComputeShader() {
        return new Shader("com/destrostudios/icetea/samples/shaders/water/butterfly.comp");
    }

    @Override
    protected int getPushConstantsSize() {
        return horizontalPushConstants[0].getData().getSize();
    }

    @Override
    public void record(VkCommandBuffer commandBuffer) {
        super.record(commandBuffer);
        recordComputeActions(commandBuffer, horizontalPushConstants);
        recordComputeActions(commandBuffer, verticalPushConstants);
    }

    private void recordComputeActions(VkCommandBuffer commandBuffer, ByteDataBuffer[] pushConstantsArray) {
        try (MemoryStack stack = stackPush()) {
            for (ByteDataBuffer pushConstants : pushConstantsArray) {
                recordComputeAction(commandBuffer, computeActions.get(0), pushConstants);
                recordComputeAction(commandBuffer, computeActions.get(1), pushConstants);
                recordComputeAction(commandBuffer, computeActions.get(2), pushConstants);

                VkMemoryBarrier.Buffer barrier = VkMemoryBarrier.callocStack(1, stack)
                        .sType(VK_STRUCTURE_TYPE_MEMORY_BARRIER)
                        .srcAccessMask(VK_ACCESS_SHADER_WRITE_BIT)
                        .dstAccessMask(VK_ACCESS_SHADER_READ_BIT);
                vkCmdPipelineBarrier(commandBuffer, VK_PIPELINE_STAGE_COMPUTE_SHADER_BIT, VK_PIPELINE_STAGE_COMPUTE_SHADER_BIT, VK_DEPENDENCY_BY_REGION_BIT, barrier, null, null);
            }
        }
    }

    private void recordComputeAction(VkCommandBuffer commandBuffer, ComputeAction computeAction, ByteDataBuffer pushConstants) {
        try (MemoryStack stack = stackPush()) {
            vkCmdPushConstants(commandBuffer, computePipeline.getPipelineLayout(), VK_SHADER_STAGE_COMPUTE_BIT, 0, pushConstants.getByteBuffer());
            vkCmdBindDescriptorSets(commandBuffer, VK_PIPELINE_BIND_POINT_COMPUTE, computePipeline.getPipelineLayout(), 0, computeAction.getResourceDescriptorSet().getDescriptorSets(0, stack), null);
            vkCmdDispatch(commandBuffer, getGroupCountX(), getGroupCountY(), getGroupCountZ());
        }
    }

    @Override
    protected int getGroupCountX() {
        return (n / 16);
    }

    @Override
    protected int getGroupCountY() {
        return (n / 16);
    }

    @Override
    protected int getGroupCountZ() {
        return 1;
    }

    @Override
    protected void cleanupInternal() {
        for (ByteDataBuffer horizontalPushConstantsData : horizontalPushConstants) {
            horizontalPushConstantsData.cleanup();
        }
        for (ByteDataBuffer verticalPushConstantsData : verticalPushConstants) {
            verticalPushConstantsData.cleanup();
        }
        super.cleanupInternal();
    }
}
