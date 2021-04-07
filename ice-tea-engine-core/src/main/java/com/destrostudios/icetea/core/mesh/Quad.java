package com.destrostudios.icetea.core.mesh;

import com.destrostudios.icetea.core.data.VertexData;
import org.joml.Vector2f;
import org.joml.Vector3f;

public class Quad extends Mesh {

    public Quad(float sizeX, float sizeY) {
        VertexData vertex1 = new VertexData();
        vertex1.setVector3f("vertexPosition", new Vector3f(0, 0, 0));
        vertex1.setVector2f("vertexTexCoord", new Vector2f(0, 1));
        vertex1.setVector3f("vertexNormal", new Vector3f(0, 0, 1));

        VertexData vertex2 = new VertexData();
        vertex2.setVector3f("vertexPosition", new Vector3f(sizeX, 0, 0));
        vertex2.setVector2f("vertexTexCoord", new Vector2f(1, 1));
        vertex2.setVector3f("vertexNormal", new Vector3f(0, 0, 1));

        VertexData vertex3 = new VertexData();
        vertex3.setVector3f("vertexPosition", new Vector3f(sizeX, sizeY, 0));
        vertex3.setVector2f("vertexTexCoord", new Vector2f(1, 0));
        vertex3.setVector3f("vertexNormal", new Vector3f(0, 0, 1));

        VertexData vertex4 = new VertexData();
        vertex4.setVector3f("vertexPosition", new Vector3f(0, sizeY, 0));
        vertex4.setVector2f("vertexTexCoord", new Vector2f(0, 0));
        vertex4.setVector3f("vertexNormal", new Vector3f(0, 0, 1));

        vertices = new VertexData[] { vertex1, vertex2, vertex3, vertex4 };
        updateBounds();

        indices = new int[] {
            0, 1, 2,
            0, 2, 3
        };
    }
}
