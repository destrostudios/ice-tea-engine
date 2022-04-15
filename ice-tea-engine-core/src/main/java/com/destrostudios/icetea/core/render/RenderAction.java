package com.destrostudios.icetea.core.render;

import org.lwjgl.vulkan.VkCommandBuffer;

public interface RenderAction {

    void render(VkCommandBuffer commandBuffer, int commandBufferIndex);
}
