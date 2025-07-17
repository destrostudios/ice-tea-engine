package com.destrostudios.icetea.core.mesh;

import com.destrostudios.icetea.core.data.VertexData;
import org.joml.Vector2f;
import org.joml.Vector3f;

public class Box extends Mesh {

    public Box() {
        this(0.5f, 0.5f, 0.5f);
    }

    public Box(float extent) {
        this(extent, extent, extent);
    }

    public Box(float extentX, float extentY, float extentZ) {
        this(extentX, extentY, extentZ, true, true);
    }

    public Box(boolean withNormals, boolean withTexCoords) {
        this(0.5f, 0.5f, 0.5f, withNormals, withTexCoords);
    }

    public Box(float extent, boolean withNormals, boolean withTexCoords) {
        this(extent, extent, extent, withNormals, withTexCoords);
    }

    public Box(float extentX, float extentY, float extentZ, boolean withNormals, boolean withTexCoords) {
        this(new Vector3f(), extentX, extentY, extentZ, withNormals, withTexCoords);
    }

    public Box(Vector3f center, float extentX, float extentY, float extentZ, boolean withNormals, boolean withTexCoords) {
        vertices = new VertexData[24];
        for (int i = 0; i < vertices.length; i++) {
            vertices[i] = new VertexData();
        }

        Vector3f[] axes = {
            new Vector3f(extentX, 0, 0),
            new Vector3f(0, extentY, 0),
            new Vector3f(0, 0, extentZ),
        };
        Vector3f[] positions = new Vector3f[] {
            new Vector3f(center).sub(axes[0]).sub(axes[1]).sub(axes[2]),
            new Vector3f(center).add(axes[0]).sub(axes[1]).sub(axes[2]),
            new Vector3f(center).add(axes[0]).add(axes[1]).sub(axes[2]),
            new Vector3f(center).sub(axes[0]).add(axes[1]).sub(axes[2]),
            new Vector3f(center).add(axes[0]).sub(axes[1]).add(axes[2]),
            new Vector3f(center).sub(axes[0]).sub(axes[1]).add(axes[2]),
            new Vector3f(center).add(axes[0]).add(axes[1]).add(axes[2]),
            new Vector3f(center).sub(axes[0]).add(axes[1]).add(axes[2])
        };

        int vertexIndex = 0;
        // Back
        vertices[vertexIndex++].setVector3f("vertexPosition", new Vector3f(positions[0].x, positions[0].y, positions[0].z));
        vertices[vertexIndex++].setVector3f("vertexPosition", new Vector3f(positions[1].x, positions[1].y, positions[1].z));
        vertices[vertexIndex++].setVector3f("vertexPosition", new Vector3f(positions[2].x, positions[2].y, positions[2].z));
        vertices[vertexIndex++].setVector3f("vertexPosition", new Vector3f(positions[3].x, positions[3].y, positions[3].z));
        // Right
        vertices[vertexIndex++].setVector3f("vertexPosition", new Vector3f(positions[1].x, positions[1].y, positions[1].z));
        vertices[vertexIndex++].setVector3f("vertexPosition", new Vector3f(positions[4].x, positions[4].y, positions[4].z));
        vertices[vertexIndex++].setVector3f("vertexPosition", new Vector3f(positions[6].x, positions[6].y, positions[6].z));
        vertices[vertexIndex++].setVector3f("vertexPosition", new Vector3f(positions[2].x, positions[2].y, positions[2].z));
        // Front
        vertices[vertexIndex++].setVector3f("vertexPosition", new Vector3f(positions[4].x, positions[4].y, positions[4].z));
        vertices[vertexIndex++].setVector3f("vertexPosition", new Vector3f(positions[5].x, positions[5].y, positions[5].z));
        vertices[vertexIndex++].setVector3f("vertexPosition", new Vector3f(positions[7].x, positions[7].y, positions[7].z));
        vertices[vertexIndex++].setVector3f("vertexPosition", new Vector3f(positions[6].x, positions[6].y, positions[6].z));
        // Left
        vertices[vertexIndex++].setVector3f("vertexPosition", new Vector3f(positions[5].x, positions[5].y, positions[5].z));
        vertices[vertexIndex++].setVector3f("vertexPosition", new Vector3f(positions[0].x, positions[0].y, positions[0].z));
        vertices[vertexIndex++].setVector3f("vertexPosition", new Vector3f(positions[3].x, positions[3].y, positions[3].z));
        vertices[vertexIndex++].setVector3f("vertexPosition", new Vector3f(positions[7].x, positions[7].y, positions[7].z));
        // Top
        vertices[vertexIndex++].setVector3f("vertexPosition", new Vector3f(positions[2].x, positions[2].y, positions[2].z));
        vertices[vertexIndex++].setVector3f("vertexPosition", new Vector3f(positions[6].x, positions[6].y, positions[6].z));
        vertices[vertexIndex++].setVector3f("vertexPosition", new Vector3f(positions[7].x, positions[7].y, positions[7].z));
        vertices[vertexIndex++].setVector3f("vertexPosition", new Vector3f(positions[3].x, positions[3].y, positions[3].z));
        // Bottom
        vertices[vertexIndex++].setVector3f("vertexPosition", new Vector3f(positions[0].x, positions[0].y, positions[0].z));
        vertices[vertexIndex++].setVector3f("vertexPosition", new Vector3f(positions[5].x, positions[5].y, positions[5].z));
        vertices[vertexIndex++].setVector3f("vertexPosition", new Vector3f(positions[4].x, positions[4].y, positions[4].z));
        vertices[vertexIndex++].setVector3f("vertexPosition", new Vector3f(positions[1].x, positions[1].y, positions[1].z));

        if (withNormals) {
            vertexIndex = 0;
            // Back
            for (int i = 0; i < 4; i++) {
                vertices[vertexIndex++].setVector3f("vertexNormal", new Vector3f(0, 0, -1));
            }
            // Right
            for (int i = 0; i < 4; i++) {
                vertices[vertexIndex++].setVector3f("vertexNormal", new Vector3f(1, 0, 0));
            }
            // Front
            for (int i = 0; i < 4; i++) {
                vertices[vertexIndex++].setVector3f("vertexNormal", new Vector3f(0, 0, 1));
            }
            // Left
            for (int i = 0; i < 4; i++) {
                vertices[vertexIndex++].setVector3f("vertexNormal", new Vector3f(-1, 0, 0));
            }
            // Top
            for (int i = 0; i < 4; i++) {
                vertices[vertexIndex++].setVector3f("vertexNormal", new Vector3f(0, 1, 0));
            }
            // Bottom
            for (int i = 0; i < 4; i++) {
                vertices[vertexIndex++].setVector3f("vertexNormal", new Vector3f(0, -1, 0));
            }
        }

        if (withTexCoords) {
            vertexIndex = 0;
            for (int i = 0; i < 6; i++) {
                vertices[vertexIndex++].setVector2f("vertexTexCoord", new Vector2f(1, 0));
                vertices[vertexIndex++].setVector2f("vertexTexCoord", new Vector2f(0, 0));
                vertices[vertexIndex++].setVector2f("vertexTexCoord", new Vector2f(0, 1));
                vertices[vertexIndex++].setVector2f("vertexTexCoord", new Vector2f(1, 1));
            }
        }

        updateBounds();

        indices = new int[] {
            // Back
            2,  1,  0,
            3,  2,  0,
            // Right
            6,  5,  4,
            7,  6,  4,
            // Front
            10,  9,  8,
            11, 10,  8,
            // Left
            14, 13, 12,
            15, 14, 12,
            // Top
            18, 17, 16,
            19, 18, 16,
            // Bottom
            22, 21, 20,
            23, 22, 20
        };
    }
}
