package com.destrostudios.icetea.core.water;

import com.destrostudios.icetea.core.data.ByteBufferData;
import com.destrostudios.icetea.core.compute.ComputeAction;
import com.destrostudios.icetea.core.compute.ComputeActionGroup;
import com.destrostudios.icetea.core.shader.Shader;
import com.destrostudios.icetea.core.material.descriptor.ComputeImageDescriptorLayout;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VkCommandBuffer;

import static org.lwjgl.vulkan.VK10.*;

public class FftInversionComputeActionGroup extends ComputeActionGroup {

    public FftInversionComputeActionGroup(int n, ByteBufferData pushConstants) {
        this.n = n;
        this.pushConstants = pushConstants;
    }
    private int n;
    private ByteBufferData pushConstants;

    @Override
    protected void fillMaterialDescriptorLayout() {
        materialDescriptorSetLayout.addDescriptorLayout(new ComputeImageDescriptorLayout());
        materialDescriptorSetLayout.addDescriptorLayout(new ComputeImageDescriptorLayout());
        materialDescriptorSetLayout.addDescriptorLayout(new ComputeImageDescriptorLayout());
    }

    @Override
    public Shader getComputeShader() {
        return new Shader("shaders/water/inversion.comp");
    }

    @Override
    protected int getPushConstantsSize() {
        return pushConstants.getSize();
    }

    @Override
    public void record(VkCommandBuffer commandBuffer, MemoryStack stack) {
        super.record(commandBuffer, stack);
        vkCmdPushConstants(commandBuffer, computePipeline.getPipelineLayout(), VK_SHADER_STAGE_COMPUTE_BIT, 0, pushConstants.getByteBuffers().get(0));
        for (ComputeAction computeAction : computeActions) {
            vkCmdBindDescriptorSets(commandBuffer, VK_PIPELINE_BIND_POINT_COMPUTE, computePipeline.getPipelineLayout(), 0, stack.longs(computeAction.getDescriptorSets().get(0)), null);
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
}
