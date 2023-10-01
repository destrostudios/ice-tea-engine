package com.destrostudios.icetea.imgui;

import com.destrostudios.icetea.core.render.GeometryRenderer;
import com.destrostudios.icetea.core.scene.Geometry;
import imgui.ImDrawData;
import imgui.ImGui;
import imgui.ImVec4;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VkCommandBuffer;
import org.lwjgl.vulkan.VkRect2D;

import static org.lwjgl.vulkan.VK10.*;

public class ImGuiGeometryRenderer extends GeometryRenderer {

    public ImGuiGeometryRenderer() {
        dynamicStates = new int[] { VK_DYNAMIC_STATE_SCISSOR };
    }
    private ImVec4 tmpClipRect = new ImVec4();

    @Override
    protected void drawVertices(Geometry geometry, VkCommandBuffer commandBuffer, MemoryStack stack) {
        ImDrawData drawData = ImGui.getDrawData();
        int commandListsCount = drawData.getCmdListsCount();
        int offsetIndex = 0;
        int offsetVertex = 0;
        VkRect2D.Buffer clipRect = VkRect2D.calloc(1, stack);
        for (int commandListIndex = 0; commandListIndex < commandListsCount; commandListIndex++) {
            int buffersCount = drawData.getCmdListCmdBufferSize(commandListIndex);
            for (int bufferIndex = 0; bufferIndex < buffersCount; bufferIndex++) {
                drawData.getCmdListCmdBufferClipRect(commandListIndex, bufferIndex, tmpClipRect);
                clipRect.offset(it -> it.x((int) tmpClipRect.x).y((int) tmpClipRect.y));
                clipRect.extent(it -> it.width((int) (tmpClipRect.z - tmpClipRect.x)).height((int) (tmpClipRect.w - tmpClipRect.y)));
                vkCmdSetScissor(commandBuffer, 0, clipRect);
                int elementsCount = drawData.getCmdListCmdBufferElemCount(commandListIndex, bufferIndex);
                vkCmdDrawIndexed(
                    commandBuffer,
                    elementsCount,
                    1,
                    offsetIndex + drawData.getCmdListCmdBufferIdxOffset(commandListIndex, bufferIndex),
                    offsetVertex + drawData.getCmdListCmdBufferVtxOffset(commandListIndex, bufferIndex),
                    0
                );
            }
            offsetIndex += drawData.getCmdListIdxBufferSize(commandListIndex);
            offsetVertex += drawData.getCmdListVtxBufferSize(commandListIndex);
        }
    }
}
