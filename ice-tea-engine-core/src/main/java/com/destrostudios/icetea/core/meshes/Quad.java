package com.destrostudios.icetea.core.meshes;

import com.destrostudios.icetea.core.Mesh;
import com.destrostudios.icetea.core.Vertex;
import org.joml.Vector2f;
import org.joml.Vector3f;

public class Quad extends Mesh {

    public Quad(float sizeX, float sizeY) {
        Vertex vertex1 = new Vertex();
        vertex1.setPosition(new Vector3f(-0.5f * sizeX, -0.5f * sizeY, 0));
        vertex1.setNormal(new Vector3f(0, 0, 1));
        vertex1.setTexCoords(new Vector2f(0, 1));
        vertex1.setColor(new Vector3f(1, 1, 1));

        Vertex vertex2 = new Vertex();
        vertex2.setPosition(new Vector3f(0.5f * sizeX, -0.5f * sizeY, 0));
        vertex2.setNormal(new Vector3f(0, 0, 1));
        vertex2.setTexCoords(new Vector2f(1, 1));
        vertex2.setColor(new Vector3f(1, 1, 1));

        Vertex vertex3 = new Vertex();
        vertex3.setPosition(new Vector3f(0.5f * sizeX, 0.5f * sizeY, 0));
        vertex3.setNormal(new Vector3f(0, 0, 1));
        vertex3.setTexCoords(new Vector2f(1, 0));
        vertex3.setColor(new Vector3f(1, 1, 1));

        Vertex vertex4 = new Vertex();
        vertex4.setPosition(new Vector3f(-0.5f * sizeX, 0.5f * sizeY, 0));
        vertex4.setNormal(new Vector3f(0, 0, 1));
        vertex4.setTexCoords(new Vector2f(0, 0));
        vertex4.setColor(new Vector3f(1, 1, 1));

        vertices = new Vertex[] { vertex1, vertex2, vertex3, vertex4 };
        indices = new int[] {
            0, 1, 2,
            0, 2, 3
        };
    }
}
