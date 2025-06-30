package com.destrostudios.icetea.samples.water;

import com.destrostudios.icetea.core.compute.ComputeActionGroup;
import com.destrostudios.icetea.core.shader.FileShader;
import com.destrostudios.icetea.core.util.MathUtil;
import com.destrostudios.icetea.core.shader.Shader;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VkCommandBuffer;

import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.vulkan.VK10.*;

public class TwiddleFactorsComputeActionGroup extends ComputeActionGroup {

    public TwiddleFactorsComputeActionGroup(int n) {
        this.n = n;
    }
    private int n;

    @Override
    public Shader getComputeShader() {
        return new FileShader("com/destrostudios/icetea/samples/shaders/water/twiddleFactors.comp");
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
        return (int) MathUtil.log2(n);
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
