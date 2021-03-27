package com.destrostudios.icetea.core.mesh;

import com.destrostudios.icetea.core.data.VertexData;
import org.joml.Vector3f;

import static org.lwjgl.vulkan.VK10.VK_PRIMITIVE_TOPOLOGY_PATCH_LIST;

public class Grid extends Mesh {

    public Grid(int patches) {
        this(patches, patches);
    }

    public Grid(int amountX, int amountY) {
        topology = VK_PRIMITIVE_TOPOLOGY_PATCH_LIST;
        vertices = new VertexData[amountX * amountY * 16];

        int index = 0;
        float dx = 1f / amountX;
        float dy = 1f / amountY;

        for (float i = 0; i < 1; i += dx) {
            for (float j = 0; j < 1; j += dy) {
                vertices[index++] = createVertex(new Vector3f(i, j, 0));
                vertices[index++] = createVertex(new Vector3f(i + dx * 0.33f, j, 0));
                vertices[index++] = createVertex(new Vector3f(i + dx * 0.66f, j, 0));
                vertices[index++] = createVertex(new Vector3f(i + dx, j, 0));

                vertices[index++] = createVertex(new Vector3f(i, j + dy * 0.33f,0));
                vertices[index++] = createVertex(new Vector3f(i + dx * 0.33f, j + dy * 0.33f, 0));
                vertices[index++] = createVertex(new Vector3f(i + dx * 0.66f, j + dy * 0.33f, 0));
                vertices[index++] = createVertex(new Vector3f(i + dx, j + dy * 0.33f, 0));

                vertices[index++] = createVertex(new Vector3f(i, j + dy * 0.66f, 0));
                vertices[index++] = createVertex(new Vector3f(i + dx * 0.33f, j + dy * 0.66f, 0));
                vertices[index++] = createVertex(new Vector3f(i + dx * 0.66f , j + dy * 0.66f, 0));
                vertices[index++] = createVertex(new Vector3f(i + dx, j + dy * 0.66f, 0));

                vertices[index++] = createVertex(new Vector3f(i, j + dy, 0));
                vertices[index++] = createVertex(new Vector3f(i + dx * 0.33f, j + dy, 0));
                vertices[index++] = createVertex(new Vector3f(i + dx * 0.66f, j + dy, 0));
                vertices[index++] = createVertex(new Vector3f(i + dx, j + dy, 0));
            }
        }
        updateBounds();
    }

    private VertexData createVertex(Vector3f position) {
        VertexData vertex = new VertexData();
        vertex.setVector3f("modelSpaceVertexPosition", position);
        return vertex;
    }
}
