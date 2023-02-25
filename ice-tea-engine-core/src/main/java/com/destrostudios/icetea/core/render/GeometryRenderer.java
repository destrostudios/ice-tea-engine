package com.destrostudios.icetea.core.render;

import com.destrostudios.icetea.core.scene.Geometry;
import lombok.Getter;
import org.lwjgl.system.MemoryStack;

import java.nio.LongBuffer;
import java.util.function.Consumer;

import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.vulkan.VK10.*;

public class GeometryRenderer {

    @Getter
    protected int[] dynamicStates;

    public <RJ extends RenderJob<?>, RP extends RenderPipeline<RJ>> void render(Geometry geometry, GeometryRenderContext<RJ, RP> renderContext, Consumer<RenderAction> actions) {
        try (MemoryStack stack = stackPush()) {
            RenderPipeline<?> renderPipeline = renderContext.getRenderPipeline();
            actions.accept(rt -> vkCmdBindPipeline(rt.getCommandBuffer(), VK_PIPELINE_BIND_POINT_GRAPHICS, renderPipeline.getPipeline()));
            LongBuffer vertexBuffers = stack.longs(geometry.getMesh().getVertexBuffer().getBuffer());
            LongBuffer offsets = stack.longs(0);
            actions.accept(rt -> vkCmdBindVertexBuffers(rt.getCommandBuffer(), 0, vertexBuffers, offsets));
            if (geometry.getMesh().getIndexBuffer() != null) {
                actions.accept(rt -> vkCmdBindIndexBuffer(rt.getCommandBuffer(), geometry.getMesh().getIndexBuffer().getBuffer(), 0, VK_INDEX_TYPE_UINT32));
            }
            actions.accept(rt -> vkCmdBindDescriptorSets(rt.getCommandBuffer(), VK_PIPELINE_BIND_POINT_GRAPHICS, renderPipeline.getPipelineLayout(), 0, renderContext.getResourceDescriptorSet().getDescriptorSets(rt.getCommandBufferIndex(), stack), null));
            drawVertices(geometry, actions, stack);
        }
    }

    protected void drawVertices(Geometry geometry, Consumer<RenderAction> actions, MemoryStack stack) {
        if (geometry.getMesh().getIndices() != null) {
            actions.accept(rt -> vkCmdDrawIndexed(rt.getCommandBuffer(), geometry.getMesh().getIndices().length, 1, 0, 0, 0));
        } else {
            actions.accept(rt -> vkCmdDraw(rt.getCommandBuffer(), geometry.getMesh().getVertices().length, 1, 0, 0));
        }
    }
}
