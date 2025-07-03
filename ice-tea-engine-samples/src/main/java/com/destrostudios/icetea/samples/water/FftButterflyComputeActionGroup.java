package com.destrostudios.icetea.samples.water;

import com.destrostudios.icetea.core.buffer.PushConstantsDataBuffer;
import com.destrostudios.icetea.core.compute.ComputeActionGroup;
import com.destrostudios.icetea.core.shader.FileShader;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VkCommandBuffer;
import org.lwjgl.vulkan.VkMemoryBarrier;

import static org.lwjgl.vulkan.VK10.*;

public class FftButterflyComputeActionGroup extends ComputeActionGroup {

    public FftButterflyComputeActionGroup(int n, PushConstantsDataBuffer[] horizontalPushConstants, PushConstantsDataBuffer[] verticalPushConstants) {
        super(new FileShader("com/destrostudios/icetea/samples/shaders/water/butterfly.comp"));
        this.n = n;
        this.horizontalPushConstants = horizontalPushConstants;
        this.verticalPushConstants = verticalPushConstants;
    }
    private int n;
    private PushConstantsDataBuffer[] horizontalPushConstants;
    private PushConstantsDataBuffer[] verticalPushConstants;

    @Override
    protected int getPushConstantsSize() {
        return horizontalPushConstants[0].getData().getSize();
    }

    @Override
    public void record(VkCommandBuffer commandBuffer, MemoryStack stack) {
        super.record(commandBuffer, stack);
        recordComputeActions(commandBuffer, horizontalPushConstants, stack);
        recordComputeActions(commandBuffer, verticalPushConstants, stack);
    }

    private void recordComputeActions(VkCommandBuffer commandBuffer, PushConstantsDataBuffer[] pushConstantsArray, MemoryStack stack) {
        for (PushConstantsDataBuffer pushConstants : pushConstantsArray) {
            recordPushConstants(commandBuffer, pushConstants);
            recordComputeAction(commandBuffer, computeActions.get(0), stack);
            recordComputeAction(commandBuffer, computeActions.get(1), stack);
            recordComputeAction(commandBuffer, computeActions.get(2), stack);

            VkMemoryBarrier.Buffer barrier = VkMemoryBarrier.callocStack(1, stack)
                    .sType(VK_STRUCTURE_TYPE_MEMORY_BARRIER)
                    .srcAccessMask(VK_ACCESS_SHADER_WRITE_BIT)
                    .dstAccessMask(VK_ACCESS_SHADER_READ_BIT);
            vkCmdPipelineBarrier(commandBuffer, VK_PIPELINE_STAGE_COMPUTE_SHADER_BIT, VK_PIPELINE_STAGE_COMPUTE_SHADER_BIT, VK_DEPENDENCY_BY_REGION_BIT, barrier, null, null);
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
    protected void cleanupNativeInternal() {
        for (PushConstantsDataBuffer horizontalPushConstantsData : horizontalPushConstants) {
            horizontalPushConstantsData.cleanupNative();
        }
        for (PushConstantsDataBuffer verticalPushConstantsData : verticalPushConstants) {
            verticalPushConstantsData.cleanupNative();
        }
        super.cleanupNativeInternal();
    }
}
