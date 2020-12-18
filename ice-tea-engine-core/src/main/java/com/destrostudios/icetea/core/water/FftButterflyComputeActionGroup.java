package com.destrostudios.icetea.core.water;

import com.destrostudios.icetea.core.*;
import com.destrostudios.icetea.core.materials.descriptors.ComputeImageDescriptorLayout;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VkCommandBuffer;
import org.lwjgl.vulkan.VkMemoryBarrier;

import static org.lwjgl.vulkan.VK10.*;

public class FftButterflyComputeActionGroup extends ComputeActionGroup {

    public FftButterflyComputeActionGroup(int n, ByteBufferData[] horizontalPushConstants, ByteBufferData[] verticalPushConstants) {
        this.n = n;
        this.horizontalPushConstants = horizontalPushConstants;
        this.verticalPushConstants = verticalPushConstants;
    }
    private int n;
    private ByteBufferData[] horizontalPushConstants;
    private ByteBufferData[] verticalPushConstants;

    @Override
    protected void fillMaterialDescriptorLayout() {
        materialDescriptorSetLayout.addDescriptorLayout(new ComputeImageDescriptorLayout());
        materialDescriptorSetLayout.addDescriptorLayout(new ComputeImageDescriptorLayout());
        materialDescriptorSetLayout.addDescriptorLayout(new ComputeImageDescriptorLayout());
    }

    @Override
    public Shader getComputeShader() {
        return new Shader("shaders/butterfly.comp");
    }

    @Override
    protected int getPushConstantsSize() {
        return horizontalPushConstants[0].getSize();
    }

    @Override
    public void record(VkCommandBuffer commandBuffer, MemoryStack stack) {
        super.record(commandBuffer, stack);
        recordComputeActions(commandBuffer, horizontalPushConstants, stack);
        recordComputeActions(commandBuffer, verticalPushConstants, stack);
    }

    private void recordComputeActions(VkCommandBuffer commandBuffer, ByteBufferData[] pushConstantsArray, MemoryStack stack) {
        for (ByteBufferData pushConstants : pushConstantsArray) {
            recordComputeAction(commandBuffer, computeActions.get(0), pushConstants, stack);
            recordComputeAction(commandBuffer, computeActions.get(1), pushConstants, stack);
            recordComputeAction(commandBuffer, computeActions.get(2), pushConstants, stack);

            VkMemoryBarrier.Buffer barrier = VkMemoryBarrier.callocStack(1, stack)
                .sType(VK_STRUCTURE_TYPE_MEMORY_BARRIER)
                .srcAccessMask(VK_ACCESS_SHADER_WRITE_BIT)
                .dstAccessMask(VK_ACCESS_SHADER_READ_BIT);
            vkCmdPipelineBarrier(commandBuffer, VK_PIPELINE_STAGE_COMPUTE_SHADER_BIT, VK_PIPELINE_STAGE_COMPUTE_SHADER_BIT, VK_DEPENDENCY_BY_REGION_BIT, barrier, null, null);
        }
    }

    private void recordComputeAction(VkCommandBuffer commandBuffer, ComputeAction computeAction, ByteBufferData pushConstants, MemoryStack stack) {
        vkCmdPushConstants(commandBuffer, computePipeline.getPipelineLayout(), VK_SHADER_STAGE_COMPUTE_BIT, 0, pushConstants.getByteBuffers().get(0));
        vkCmdBindDescriptorSets(commandBuffer, VK_PIPELINE_BIND_POINT_COMPUTE, computePipeline.getPipelineLayout(), 0, stack.longs(computeAction.getDescriptorSets().get(0)), null);
        vkCmdDispatch(commandBuffer, getGroupCountX(), getGroupCountY(), getGroupCountZ());
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
}
