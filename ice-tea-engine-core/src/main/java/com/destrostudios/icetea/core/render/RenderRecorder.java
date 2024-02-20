package com.destrostudios.icetea.core.render;

import com.destrostudios.icetea.core.Pipeline;
import com.destrostudios.icetea.core.buffer.StagedResizableMemoryBuffer;
import com.destrostudios.icetea.core.resource.ResourceDescriptorSet;
import lombok.Getter;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VkCommandBuffer;

import java.nio.LongBuffer;

import static org.lwjgl.vulkan.VK10.*;

public class RenderRecorder {

	public RenderRecorder(int imageIndex, int frameBufferIndex, VkCommandBuffer commandBuffer) {
		this.imageIndex = imageIndex;
		this.frameBufferIndex = frameBufferIndex;
		this.commandBuffer = commandBuffer;
	}
	private int imageIndex;
	@Getter
	private int frameBufferIndex;
	@Getter
	private VkCommandBuffer commandBuffer;
	private Pipeline boundPipeline;
	private ResourceDescriptorSet boundResourceDescriptorSet;
	private StagedResizableMemoryBuffer boundVertexBuffer;
	private StagedResizableMemoryBuffer boundIndexBuffer;

	public void bindPipeline(Pipeline pipeline) {
		if (pipeline != boundPipeline) {
			vkCmdBindPipeline(commandBuffer, VK_PIPELINE_BIND_POINT_GRAPHICS, pipeline.getPipeline());
			boundPipeline = pipeline;
		}
	}

	public void bindVertexBuffer(StagedResizableMemoryBuffer vertexBuffer, MemoryStack stack) {
		if (vertexBuffer != boundVertexBuffer) {
			LongBuffer pBuffer = stack.longs(vertexBuffer.getBuffer());
			LongBuffer offsets = stack.longs(0);
			vkCmdBindVertexBuffers(commandBuffer, 0, pBuffer, offsets);
			boundVertexBuffer = vertexBuffer;
		}
	}

	public void bindIndexBuffer(StagedResizableMemoryBuffer indexBuffer) {
		if (indexBuffer != boundIndexBuffer) {
			vkCmdBindIndexBuffer(commandBuffer, indexBuffer.getBuffer(), 0, VK_INDEX_TYPE_UINT32);
			boundIndexBuffer = indexBuffer;
		}
	}

	public void bindDescriptorSets(ResourceDescriptorSet resourceDescriptorSet, MemoryStack stack) {
		for (int i = 0; i < resourceDescriptorSet.size(); i++) {
			long descriptorSet = resourceDescriptorSet.getDescriptorSet(i, imageIndex);
			Long boundDescriptorSet = null;
			if ((boundResourceDescriptorSet != null) && (boundResourceDescriptorSet.size() > i)) {
				boundDescriptorSet = boundResourceDescriptorSet.getDescriptorSet(i, imageIndex);
			}
			if ((boundDescriptorSet == null) || (descriptorSet != boundDescriptorSet)) {
				vkCmdBindDescriptorSets(commandBuffer, VK_PIPELINE_BIND_POINT_GRAPHICS, boundPipeline.getPipelineLayout(), i, resourceDescriptorSet.getDescriptorSets(i, imageIndex, stack), null);
				break;
			}
		}
		boundResourceDescriptorSet = resourceDescriptorSet;
	}
}
