package com.destrostudios.icetea.samples.water;

import com.destrostudios.icetea.core.buffer.PushConstantsDataBuffer;
import com.destrostudios.icetea.core.compute.ComputeAction;
import com.destrostudios.icetea.core.compute.ComputeActionGroup;
import com.destrostudios.icetea.core.shader.FileShader;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VkCommandBuffer;

public class FftInversionComputeActionGroup extends ComputeActionGroup {

    public FftInversionComputeActionGroup(int n, PushConstantsDataBuffer pushConstants) {
        super(new FileShader("com/destrostudios/icetea/samples/shaders/water/inversion.comp"));
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
        for (ComputeAction computeAction : computeActions) {
            recordComputeAction(commandBuffer, computeAction, stack);
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
        pushConstants.cleanupNative();
        super.cleanupNativeInternal();
    }
}
