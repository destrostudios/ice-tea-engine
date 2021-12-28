package com.destrostudios.icetea.samples.water;

import com.destrostudios.icetea.core.compute.ComputeActionGroup;
import com.destrostudios.icetea.core.shader.Shader;
import com.destrostudios.icetea.core.material.descriptor.ComputeImageDescriptorLayout;
import com.destrostudios.icetea.core.material.descriptor.UniformDescriptorLayout;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VkCommandBuffer;

import static org.lwjgl.vulkan.VK10.*;

public class H0kComputeActionGroup extends ComputeActionGroup {

    public H0kComputeActionGroup(int n) {
        this.n = n;
    }
    private int n;

    @Override
    protected void fillMaterialDescriptorLayout() {
        materialDescriptorSetLayout.addDescriptorLayout(new ComputeImageDescriptorLayout());
        materialDescriptorSetLayout.addDescriptorLayout(new ComputeImageDescriptorLayout());
        materialDescriptorSetLayout.addDescriptorLayout(new ComputeImageDescriptorLayout());
        materialDescriptorSetLayout.addDescriptorLayout(new ComputeImageDescriptorLayout());
        materialDescriptorSetLayout.addDescriptorLayout(new ComputeImageDescriptorLayout());
        materialDescriptorSetLayout.addDescriptorLayout(new ComputeImageDescriptorLayout());
        materialDescriptorSetLayout.addDescriptorLayout(new UniformDescriptorLayout(VK_SHADER_STAGE_COMPUTE_BIT));
    }

    @Override
    public Shader getComputeShader() {
        return new Shader("com/destrostudios/icetea/samples/shaders/water/h0k.comp");
    }

    @Override
    public void record(VkCommandBuffer commandBuffer, MemoryStack stack) {
        super.record(commandBuffer, stack);
        vkCmdBindDescriptorSets(commandBuffer, VK_PIPELINE_BIND_POINT_COMPUTE, computePipeline.getPipelineLayout(), 0, stack.longs(computeActions.get(0).getDescriptorSets().get(0)), null);
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
