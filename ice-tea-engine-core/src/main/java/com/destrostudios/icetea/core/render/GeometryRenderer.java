package com.destrostudios.icetea.core.render;

import com.destrostudios.icetea.core.mesh.Mesh;
import lombok.Getter;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VkCommandBuffer;

import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.vulkan.VK10.*;

public class GeometryRenderer {

    @Getter
    protected int[] dynamicStates;

    public <RJ extends RenderJob<?>> void render(RenderRecorder recorder, GeometryRenderContext<RJ> geometryRenderContext) {
        try (MemoryStack stack = stackPush()) {
            Mesh mesh = geometryRenderContext.getGeometry().getMesh();
            recorder.bindPipeline(geometryRenderContext.getRenderPipeline());
            recorder.bindVertexBuffer(mesh.getVertexBuffer(), stack);
            if (mesh.getIndexBuffer() != null) {
                recorder.bindIndexBuffer(mesh.getIndexBuffer());
            }
            recorder.bindDescriptorSets(geometryRenderContext.getResourceDescriptorSet(), stack);
            drawVertices(recorder.getCommandBuffer(), mesh, stack);
        }
    }

    protected void drawVertices(VkCommandBuffer commandBuffer, Mesh mesh, MemoryStack stack) {
        if (mesh.getIndices() != null) {
            vkCmdDrawIndexed(commandBuffer, mesh.getIndices().length, 1, 0, 0, 0);
        } else {
            vkCmdDraw(commandBuffer, mesh.getVertices().length, 1, 0, 0);
        }
    }
}
