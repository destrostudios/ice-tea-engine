package com.destrostudios.icetea.core.render;

import com.destrostudios.icetea.core.Pipeline;
import com.destrostudios.icetea.core.buffer.StagedResizableMemoryBuffer;
import com.destrostudios.icetea.core.resource.ResourceDescriptorSet;
import lombok.Getter;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VkClearValue;
import org.lwjgl.vulkan.VkCommandBuffer;
import org.lwjgl.vulkan.VkRenderPassBeginInfo;

import java.nio.LongBuffer;

import static org.lwjgl.vulkan.VK10.*;

public class RenderRecorder {

	public RenderRecorder(int imageIndex, long frameBuffer, int frameBufferIndex, VkCommandBuffer commandBuffer, boolean isPrimaryCommandBuffer) {
		this.imageIndex = imageIndex;
		this.frameBuffer = frameBuffer;
		this.frameBufferIndex = frameBufferIndex;
		this.commandBuffer = commandBuffer;
		this.isPrimaryCommandBuffer = isPrimaryCommandBuffer;
	}
	private int imageIndex;
	private long frameBuffer;
	@Getter
	private int frameBufferIndex;
	@Getter
	private VkCommandBuffer commandBuffer;
	private boolean isPrimaryCommandBuffer;
	private Pipeline boundPipeline;
	private ResourceDescriptorSet boundResourceDescriptorSet;
	private StagedResizableMemoryBuffer boundVertexBuffer;
	private StagedResizableMemoryBuffer boundIndexBuffer;

	public void beginRenderPass(RenderJob<?> renderJob, MemoryStack stack) {
		VkRenderPassBeginInfo renderPassBeginInfo = VkRenderPassBeginInfo.callocStack(stack);
		renderPassBeginInfo.sType(VK_STRUCTURE_TYPE_RENDER_PASS_BEGIN_INFO);
		renderPassBeginInfo.renderPass(renderJob.getRenderPass());
		renderPassBeginInfo.renderArea(renderJob.getRenderArea(stack));
		VkClearValue.Buffer clearValues = renderJob.getClearValues(stack);
		if (clearValues != null) {
			renderPassBeginInfo.pClearValues(clearValues);
		}
		renderPassBeginInfo.framebuffer(frameBuffer);
		vkCmdBeginRenderPass(commandBuffer, renderPassBeginInfo, isPrimaryCommandBuffer ? VK_SUBPASS_CONTENTS_INLINE : VK_SUBPASS_CONTENTS_SECONDARY_COMMAND_BUFFERS);
	}

	public void endRenderPass() {
		vkCmdEndRenderPass(commandBuffer);
	}

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
