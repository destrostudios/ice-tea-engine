package com.destrostudios.icetea.samples.water;

import com.destrostudios.icetea.core.compute.ComputeActionGroup;
import com.destrostudios.icetea.core.shader.FileShader;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VkCommandBuffer;

public class HktComputeActionGroup extends ComputeActionGroup {

    public HktComputeActionGroup(int n) {
        super(new FileShader("com/destrostudios/icetea/samples/shaders/water/hkt.comp"));
        this.n = n;
    }
    private int n;

    @Override
    public void record(VkCommandBuffer commandBuffer, MemoryStack stack) {
        super.record(commandBuffer, stack);
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
}
