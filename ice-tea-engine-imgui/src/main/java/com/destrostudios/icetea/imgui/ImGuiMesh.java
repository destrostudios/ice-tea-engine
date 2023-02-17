package com.destrostudios.icetea.imgui;

import com.destrostudios.icetea.core.data.VertexData;
import com.destrostudios.icetea.core.mesh.Mesh;
import imgui.ImDrawData;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector4f;

import java.nio.ByteBuffer;

public class ImGuiMesh extends Mesh {

    public void update(ImDrawData drawData) {
        vertices = new VertexData[drawData.getTotalVtxCount()];
        indices = new int[drawData.getTotalIdxCount()];
        int vertexIndex = 0;
        int indexIndex = 0;
        int commandListsCount = drawData.getCmdListsCount();
        for (int commandListIndex = 0; commandListIndex < commandListsCount; commandListIndex++) {
            int verticesCount = drawData.getCmdListVtxBufferSize(commandListIndex);
            ByteBuffer vertexBuffer = drawData.getCmdListVtxBufferData(commandListIndex);
            for (int i = 0; i < verticesCount; i++) {
                float positionX = vertexBuffer.getFloat();
                float positionY = vertexBuffer.getFloat();
                float texCoordX = vertexBuffer.getFloat();
                float texCoordY = vertexBuffer.getFloat();
                float colorR = readColorComponent(vertexBuffer);
                float colorG = readColorComponent(vertexBuffer);
                float colorB = readColorComponent(vertexBuffer);
                float colorA = readColorComponent(vertexBuffer);
                VertexData vertex = new VertexData();
                vertex.setVector3f("vertexPosition", new Vector3f(positionX, positionY, 0));
                vertex.setVector2f("vertexTexCoord", new Vector2f(texCoordX, texCoordY));
                vertex.setVector4f("vertexColor", new Vector4f(colorR, colorG, colorB, colorA));
                vertices[vertexIndex++] = vertex;
            }
            int indicesCount = drawData.getCmdListIdxBufferSize(commandListIndex);
            ByteBuffer indexBuffer = drawData.getCmdListIdxBufferData(commandListIndex);
            for (int i = 0; i < indicesCount; i++) {
                indices[indexIndex++] = indexBuffer.getShort();
            }
        }
        setBuffersOutdated();
        updateBounds();
    }

    private float readColorComponent(ByteBuffer byteBuffer) {
        return (Byte.toUnsignedInt(byteBuffer.get()) / 255f);
    }
}
