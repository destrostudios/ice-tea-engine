package com.destrostudios.icetea.samples.water;

import com.destrostudios.icetea.core.buffer.PushConstantsDataBuffer;
import com.destrostudios.icetea.core.compute.ComputeActionGroup;
import com.destrostudios.icetea.core.shader.FileShader;
import com.destrostudios.icetea.core.shader.Shader;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VkCommandBuffer;

import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.vulkan.VK10.*;

public class NormalMapComputeActionGroup extends ComputeActionGroup {

    public NormalMapComputeActionGroup(int n, PushConstantsDataBuffer pushConstants) {
        this.n = n;
        this.pushConstants = pushConstants;
    }
    private int n;
    private PushConstantsDataBuffer pushConstants;

    @Override
    public Shader getComputeShader() {
        return new FileShader("com/destrostudios/icetea/samples/shaders/water/normals.comp");
    }

    @Override
    protected int getPushConstantsSize() {
        return pushConstants.getData().getSize();
    }

    @Override
    public void record(VkCommandBuffer commandBuffer) {
        super.record(commandBuffer);
        try (MemoryStack stack = stackPush()) {
            vkCmdPushConstants(commandBuffer, computePipeline.getPipelineLayout(), VK_SHADER_STAGE_COMPUTE_BIT, 0, pushConstants.getBuffer().getByteBuffer());
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

    @Override
    protected void cleanupNativeInternal() {
        pushConstants.cleanupNative();
        super.cleanupNativeInternal();
    }
}
