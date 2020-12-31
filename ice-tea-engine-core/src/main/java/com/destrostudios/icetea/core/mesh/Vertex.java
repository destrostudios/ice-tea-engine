package com.destrostudios.icetea.core.mesh;

import lombok.Getter;
import lombok.Setter;
import org.joml.Vector2fc;
import org.joml.Vector3fc;

@Getter
@Setter
public class Vertex {
    private Vector3fc position;
    private Vector3fc color;
    private Vector2fc texCoords;
    private Vector3fc normal;
}
