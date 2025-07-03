package com.destrostudios.icetea.samples.water;

import com.destrostudios.icetea.core.buffer.PushConstantsDataBuffer;
import com.destrostudios.icetea.core.compute.ComputeActionGroup;
import com.destrostudios.icetea.core.shader.FileShader;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VkCommandBuffer;

public class NormalMapComputeActionGroup extends ComputeActionGroup {

    public NormalMapComputeActionGroup(int n, PushConstantsDataBuffer pushConstants) {
        super(new FileShader("com/destrostudios/icetea/samples/shaders/water/normals.comp"));
        this.n = n;
        this.pushConstants = pushConstants;
    }
    private int n;
    private PushConstantsDataBuffer pushConstants;

    @Override
    protected int getPushConstantsSize() {
        return pushConstants.getData().getSize();
    }

    @Override
    public void record(VkCommandBuffer commandBuffer, MemoryStack stack) {
        super.record(commandBuffer, stack);
        recordPushConstants(commandBuffer, pushConstants);
        recordComputeAction(commandBuffer, computeActions.get(0), stack);
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
        pushConstants.cleanupNative();
        super.cleanupNativeInternal();
    }
}
