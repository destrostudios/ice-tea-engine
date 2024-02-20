package com.destrostudios.icetea.samples.water;

import com.destrostudios.icetea.core.compute.ComputeActionGroup;
import com.destrostudios.icetea.core.shader.Shader;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VkCommandBuffer;

import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.vulkan.VK10.*;

public class H0kComputeActionGroup extends ComputeActionGroup {

    public H0kComputeActionGroup(int n) {
        this.n = n;
    }
    private int n;

    @Override
    public Shader getComputeShader() {
        return new Shader("com/destrostudios/icetea/samples/shaders/water/h0k.comp");
    }

    @Override
    public void record(VkCommandBuffer commandBuffer) {
        super.record(commandBuffer);
        try (MemoryStack stack = stackPush()) {
            vkCmdBindDescriptorSets(commandBuffer, VK_PIPELINE_BIND_POINT_COMPUTE, computePipeline.getPipelineLayout(), 0, computeActions.get(0).getResourceDescriptorSet().getDescriptorSets(0, 0, stack), null);
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
