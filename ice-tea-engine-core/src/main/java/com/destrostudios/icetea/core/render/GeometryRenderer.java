package com.destrostudios.icetea.core.render;

import com.destrostudios.icetea.core.scene.Geometry;
import lombok.Getter;
import org.lwjgl.system.MemoryStack;

import java.nio.LongBuffer;

import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.vulkan.VK10.*;

public class GeometryRenderer {

    @Getter
    protected int[] dynamicStates;

    public <RJ extends RenderJob<?>, RP extends RenderPipeline<RJ>> void render(Geometry geometry, GeometryRenderContext<RJ, RP> renderContext, RenderTarget renderTarget) {
        try (MemoryStack stack = stackPush()) {
            RenderPipeline<?> renderPipeline = renderContext.getRenderPipeline();
            vkCmdBindPipeline(renderTarget.getCommandBuffer(), VK_PIPELINE_BIND_POINT_GRAPHICS, renderPipeline.getPipeline());
            LongBuffer vertexBuffers = stack.longs(geometry.getMesh().getVertexBuffer().getBuffer());
            LongBuffer offsets = stack.longs(0);
            vkCmdBindVertexBuffers(renderTarget.getCommandBuffer(), 0, vertexBuffers, offsets);
            if (geometry.getMesh().getIndexBuffer() != null) {
                vkCmdBindIndexBuffer(renderTarget.getCommandBuffer(), geometry.getMesh().getIndexBuffer().getBuffer(), 0, VK_INDEX_TYPE_UINT32);
            }
            vkCmdBindDescriptorSets(renderTarget.getCommandBuffer(), VK_PIPELINE_BIND_POINT_GRAPHICS, renderPipeline.getPipelineLayout(), 0, renderContext.getResourceDescriptorSet().getDescriptorSets(renderTarget.getImageIndex(), stack), null);
            drawVertices(geometry, renderTarget, stack);
        }
    }

    protected void drawVertices(Geometry geometry, RenderTarget renderTarget, MemoryStack stack) {
        if (geometry.getMesh().getIndices() != null) {
            vkCmdDrawIndexed(renderTarget.getCommandBuffer(), geometry.getMesh().getIndices().length, 1, 0, 0, 0);
        } else {
            vkCmdDraw(renderTarget.getCommandBuffer(), geometry.getMesh().getVertices().length, 1, 0, 0);
        }
    }
}
