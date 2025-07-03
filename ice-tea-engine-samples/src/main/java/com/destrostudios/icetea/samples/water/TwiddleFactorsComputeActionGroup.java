package com.destrostudios.icetea.samples.water;

import com.destrostudios.icetea.core.compute.ComputeActionGroup;
import com.destrostudios.icetea.core.shader.FileShader;
import com.destrostudios.icetea.core.util.MathUtil;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VkCommandBuffer;

public class TwiddleFactorsComputeActionGroup extends ComputeActionGroup {

    public TwiddleFactorsComputeActionGroup(int n) {
        super(new FileShader("com/destrostudios/icetea/samples/shaders/water/twiddleFactors.comp"));
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
        return (int) MathUtil.log2(n);
    }

    @Override
    protected int getGroupCountY() {
        return (n / 16);
    }
}
