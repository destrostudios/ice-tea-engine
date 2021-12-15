package com.destrostudios.icetea.core.mesh;

import com.destrostudios.icetea.core.data.VertexData;
import org.joml.Vector3f;

import static org.lwjgl.vulkan.VK10.VK_PRIMITIVE_TOPOLOGY_PATCH_LIST;

public class Grid extends Mesh {

    public Grid(int patches) {
        this(patches, patches);
    }

    public Grid(int amountX, int amountZ) {
        this(amountX, amountZ, new float[amountX + 1][amountZ + 1]);
    }

    public Grid(int amountX, int amountZ, float[][] height) {
        topology = VK_PRIMITIVE_TOPOLOGY_PATCH_LIST;
        vertices = new VertexData[amountX * amountZ * 16];

        int index = 0;
        float dx = 1f / amountX;
        float dz = 1f / amountZ;

        float x;
        float z;

        for (int i = 0; i < amountX; i++) {
            for (int j = 0; j < amountZ; j++) {
                x = (i * dx);
                z = (j * dz);

                vertices[index++] = createVertex(new Vector3f(x, height[i][j], z));
                vertices[index++] = createVertex(new Vector3f(x + dx * 0.33f, height[i][j], z));
                vertices[index++] = createVertex(new Vector3f(x + dx * 0.66f, height[i][j], z));
                vertices[index++] = createVertex(new Vector3f(x + dx, height[i + 1][j], z));

                vertices[index++] = createVertex(new Vector3f(x, height[i][j], z + dz * 0.33f));
                vertices[index++] = createVertex(new Vector3f(x + dx * 0.33f, height[i][j], z + dz * 0.33f));
                vertices[index++] = createVertex(new Vector3f(x + dx * 0.66f, height[i][j], z + dz * 0.33f));
                vertices[index++] = createVertex(new Vector3f(x + dx, height[i + 1][j], z + dz * 0.33f));

                vertices[index++] = createVertex(new Vector3f(x, height[i][j], z + dz * 0.66f));
                vertices[index++] = createVertex(new Vector3f(x + dx * 0.33f, height[i][j], z + dz * 0.66f));
                vertices[index++] = createVertex(new Vector3f(x + dx * 0.66f, height[i][j], z + dz * 0.66f));
                vertices[index++] = createVertex(new Vector3f(x + dx, height[i + 1][j], z + dz * 0.66f));

                vertices[index++] = createVertex(new Vector3f(x, height[i][j + 1], z + dz));
                vertices[index++] = createVertex(new Vector3f(x + dx * 0.33f, height[i][j + 1], z + dz));
                vertices[index++] = createVertex(new Vector3f(x + dx * 0.66f, height[i][j + 1], z + dz));
                vertices[index++] = createVertex(new Vector3f(x + dx, height[i + 1][j + 1], z + dz));
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
