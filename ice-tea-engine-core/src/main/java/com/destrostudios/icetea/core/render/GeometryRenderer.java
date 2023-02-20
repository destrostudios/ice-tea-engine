package com.destrostudios.icetea.core.render;

import com.destrostudios.icetea.core.scene.Geometry;
import lombok.Getter;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VkCommandBuffer;

import java.nio.LongBuffer;

import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.vulkan.VK10.*;

public class GeometryRenderer {

    @Getter
    protected int[] dynamicStates;

    public <RJ extends RenderJob<?>, RP extends RenderPipeline<RJ>> void render(VkCommandBuffer commandBuffer, int commandBufferIndex, Geometry geometry, GeometryRenderContext<RJ, RP> renderContext) {
        try (MemoryStack stack = stackPush()) {
            RenderPipeline<?> renderPipeline = renderContext.getRenderPipeline();
            vkCmdBindPipeline(commandBuffer, VK_PIPELINE_BIND_POINT_GRAPHICS, renderPipeline.getPipeline());
            LongBuffer vertexBuffers = stack.longs(geometry.getMesh().getVertexBuffer().getBuffer());
            LongBuffer offsets = stack.longs(0);
            vkCmdBindVertexBuffers(commandBuffer, 0, vertexBuffers, offsets);
            if (geometry.getMesh().getIndexBuffer() != null) {
                vkCmdBindIndexBuffer(commandBuffer, geometry.getMesh().getIndexBuffer().getBuffer(), 0, VK_INDEX_TYPE_UINT32);
            }
            vkCmdBindDescriptorSets(commandBuffer, VK_PIPELINE_BIND_POINT_GRAPHICS, renderPipeline.getPipelineLayout(), 0, renderContext.getResourceDescriptorSet().getDescriptorSets(commandBufferIndex, stack), null);
            drawVertices(commandBuffer, commandBufferIndex, geometry, stack);
        }
    }

    protected void drawVertices(VkCommandBuffer commandBuffer, int commandBufferIndex, Geometry geometry, MemoryStack stack) {
        if (geometry.getMesh().getIndices() != null) {
            vkCmdDrawIndexed(commandBuffer, geometry.getMesh().getIndices().length, 1, 0, 0, 0);
        } else {
            vkCmdDraw(commandBuffer, geometry.getMesh().getVertices().length, 1, 0, 0);
        }
    }
}
