package com.destrostudios.icetea.core.mesh;

import com.destrostudios.icetea.core.data.VertexData;
import org.joml.Vector3f;

import static org.lwjgl.vulkan.VK10.VK_PRIMITIVE_TOPOLOGY_PATCH_LIST;

public class Grid extends Mesh {

    public Grid(int patches) {
        this(patches, patches);
    }

    public Grid(int amountX, int amountY) {
        this(amountX, amountY, new float[amountX + 1][amountY + 1]);
    }

    public Grid(int amountX, int amountY, float[][] height) {
        topology = VK_PRIMITIVE_TOPOLOGY_PATCH_LIST;
        vertices = new VertexData[amountX * amountY * 16];

        int index = 0;
        float dx = 1f / amountX;
        float dy = 1f / amountY;

        float x;
        float y;

        for (int i = 0; i < amountX; i++) {
            for (int j = 0; j < amountY; j++) {
                x = (i * dx);
                y = (j * dy);

                vertices[index++] = createVertex(new Vector3f(x, y, height[i][j]));
                vertices[index++] = createVertex(new Vector3f(x + dx * 0.33f, y, height[i][j]));
                vertices[index++] = createVertex(new Vector3f(x + dx * 0.66f, y, height[i][j]));
                vertices[index++] = createVertex(new Vector3f(x + dx, y, height[i + 1][j]));

                vertices[index++] = createVertex(new Vector3f(x, y + dy * 0.33f, height[i][j]));
                vertices[index++] = createVertex(new Vector3f(x + dx * 0.33f, y + dy * 0.33f, height[i][j]));
                vertices[index++] = createVertex(new Vector3f(x + dx * 0.66f, y + dy * 0.33f, height[i][j]));
                vertices[index++] = createVertex(new Vector3f(x + dx, y + dy * 0.33f, height[i + 1][j]));

                vertices[index++] = createVertex(new Vector3f(x, y + dy * 0.66f, height[i][j]));
                vertices[index++] = createVertex(new Vector3f(x + dx * 0.33f, y + dy * 0.66f, height[i][j]));
                vertices[index++] = createVertex(new Vector3f(x + dx * 0.66f , y + dy * 0.66f, height[i][j]));
                vertices[index++] = createVertex(new Vector3f(x + dx, y + dy * 0.66f, height[i + 1][j]));

                vertices[index++] = createVertex(new Vector3f(x, y + dy, height[i][j + 1]));
                vertices[index++] = createVertex(new Vector3f(x + dx * 0.33f, y + dy, height[i][j + 1]));
                vertices[index++] = createVertex(new Vector3f(x + dx * 0.66f, y + dy, height[i][j + 1]));
                vertices[index++] = createVertex(new Vector3f(x + dx, y + dy, height[i + 1][j + 1]));
            }
        }
        updateBounds();
    }

    private VertexData createVertex(Vector3f position) {
        VertexData vertex = new VertexData();
        vertex.setVector3f("vertexPosition", position);
        return vertex;
    }
}
