package com.destrostudios.icetea.core.render;

import org.lwjgl.vulkan.VkCommandBuffer;

public interface RenderTask {

	void render(VkCommandBuffer commandBuffer, RenderContext renderContext);
}
