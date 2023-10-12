package com.destrostudios.icetea.core.render;

import com.destrostudios.icetea.core.Pipeline;
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

    public <RJ extends RenderJob<?, ?>> void render(Geometry geometry, GeometryRenderContext<RJ> geometryRenderContext, VkCommandBuffer commandBuffer, RenderContext renderContext) {
        try (MemoryStack stack = stackPush()) {
            Pipeline renderPipeline = geometryRenderContext.getRenderPipeline();
            vkCmdBindPipeline(commandBuffer, VK_PIPELINE_BIND_POINT_GRAPHICS, renderPipeline.getPipeline());
            LongBuffer vertexBuffers = stack.longs(geometry.getMesh().getVertexBuffer().getBuffer());
            LongBuffer offsets = stack.longs(0);
            vkCmdBindVertexBuffers(commandBuffer, 0, vertexBuffers, offsets);
            if (geometry.getMesh().getIndexBuffer() != null) {
                vkCmdBindIndexBuffer(commandBuffer, geometry.getMesh().getIndexBuffer().getBuffer(), 0, VK_INDEX_TYPE_UINT32);
            }
            vkCmdBindDescriptorSets(commandBuffer, VK_PIPELINE_BIND_POINT_GRAPHICS, renderPipeline.getPipelineLayout(), 0, geometryRenderContext.getResourceDescriptorSet().getDescriptorSets(renderContext.getImageIndex(), stack), null);
            drawVertices(geometry, commandBuffer, stack);
        }
    }

    protected void drawVertices(Geometry geometry, VkCommandBuffer commandBuffer, MemoryStack stack) {
        if (geometry.getMesh().getIndices() != null) {
            vkCmdDrawIndexed(commandBuffer, geometry.getMesh().getIndices().length, 1, 0, 0, 0);
        } else {
            vkCmdDraw(commandBuffer, geometry.getMesh().getVertices().length, 1, 0, 0);
        }
    }
}
