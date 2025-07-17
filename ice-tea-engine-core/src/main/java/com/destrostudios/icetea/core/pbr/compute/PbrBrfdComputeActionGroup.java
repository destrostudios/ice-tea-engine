package com.destrostudios.icetea.core.pbr.compute;

import com.destrostudios.icetea.core.compute.ComputeActionGroup;
import com.destrostudios.icetea.core.shader.FileShader;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VkCommandBuffer;

public class PbrBrfdComputeActionGroup extends ComputeActionGroup {

    public PbrBrfdComputeActionGroup(int textureSize) {
        super(new FileShader("com/destrostudios/icetea/core/shaders/pbr/brdf.comp"));
        this.textureSize = textureSize;
    }
    private int textureSize;

    @Override
    public void record(VkCommandBuffer commandBuffer, MemoryStack stack) {
        super.record(commandBuffer, stack);
        recordComputeAction(commandBuffer, computeActions.get(0), stack);
    }

    @Override
    protected int getGroupCountX() {
        return textureSize / 16;
    }

    @Override
    protected int getGroupCountY() {
        return textureSize / 16;
    }
}
