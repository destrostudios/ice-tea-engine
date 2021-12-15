package com.destrostudios.icetea.core.mesh;

import com.destrostudios.icetea.core.data.VertexData;
import org.joml.Vector2f;
import org.joml.Vector3f;

public class Box extends Mesh {

    public Box(float sizeX, float sizeY, float sizeZ) {
        // TODO: This mesh should be built different (in respect to correct normals)
        vertices = new VertexData[8];
        int vertexIndex = 0;
        for (int i = 0; i < 2; i++) {
            float z = (i * sizeZ);

            VertexData vertex1 = new VertexData();
            vertex1.setVector3f("vertexPosition", new Vector3f(0, 0, z));
            vertex1.setVector2f("vertexTexCoord", new Vector2f(0, 1));
            vertex1.setVector3f("vertexNormal", new Vector3f(0, 1, 0));
            vertices[vertexIndex++] = vertex1;

            VertexData vertex2 = new VertexData();
            vertex2.setVector3f("vertexPosition", new Vector3f(sizeX, 0, z));
            vertex2.setVector2f("vertexTexCoord", new Vector2f(1, 1));
            vertex2.setVector3f("vertexNormal", new Vector3f(0, 1, 0));
            vertices[vertexIndex++] = vertex2;

            VertexData vertex3 = new VertexData();
            vertex3.setVector3f("vertexPosition", new Vector3f(sizeX, sizeY, z));
            vertex3.setVector2f("vertexTexCoord", new Vector2f(1, 0));
            vertex3.setVector3f("vertexNormal", new Vector3f(0, 1, 0));
            vertices[vertexIndex++] = vertex3;

            VertexData vertex4 = new VertexData();
            vertex4.setVector3f("vertexPosition", new Vector3f(0, sizeY, z));
            vertex4.setVector2f("vertexTexCoord", new Vector2f(0, 0));
            vertex4.setVector3f("vertexNormal", new Vector3f(0, 1, 0));
            vertices[vertexIndex++] = vertex4;
        }
        updateBounds();

        indices = new int[] {
            // Bottom
            0, 2, 1,
            0, 3, 2,
            // Left
            0, 7, 3,
            0, 4, 7,
            // Right
            1, 2, 6,
            1, 6, 5,
            // Front
            0, 1, 5,
            0, 5, 4,
            // Back
            3, 6, 2,
            3, 7, 6,
            // Top
            4, 5, 6,
            4, 6, 7
        };
    }
}
