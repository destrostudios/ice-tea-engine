package com.destrostudios.icetea.core.render;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.lwjgl.vulkan.VkCommandBuffer;

@AllArgsConstructor
@Getter
public class RenderTarget {
	private VkCommandBuffer commandBuffer;
	private int imageIndex;
	private long frameBuffer;
	private int frameBufferIndex;
}
