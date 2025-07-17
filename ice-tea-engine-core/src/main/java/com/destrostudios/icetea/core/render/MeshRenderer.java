package com.destrostudios.icetea.core.render;

import com.destrostudios.icetea.core.mesh.Mesh;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VkCommandBuffer;

import static org.lwjgl.vulkan.VK10.vkCmdDraw;
import static org.lwjgl.vulkan.VK10.vkCmdDrawIndexed;

public class MeshRenderer {

    public void drawMesh(RenderRecorder recorder, Mesh mesh, MemoryStack stack) {
        recorder.bindVertexBuffer(mesh.getVertexBuffer(), stack);
        if (mesh.getIndexBuffer() != null) {
            recorder.bindIndexBuffer(mesh.getIndexBuffer());
        }
        drawVertices(recorder.getCommandBuffer(), mesh, stack);
    }

    protected void drawVertices(VkCommandBuffer commandBuffer, Mesh mesh, MemoryStack stack) {
        if (mesh.getIndices() != null) {
            vkCmdDrawIndexed(commandBuffer, mesh.getIndices().length, 1, 0, 0, 0);
        } else {
            vkCmdDraw(commandBuffer, mesh.getVertices().length, 1, 0, 0);
        }
    }
}
