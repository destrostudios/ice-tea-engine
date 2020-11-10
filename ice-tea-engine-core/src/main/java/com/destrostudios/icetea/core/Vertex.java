package com.destrostudios.icetea.core;

import lombok.Getter;
import lombok.Setter;
import org.joml.Vector2fc;
import org.joml.Vector3fc;

@Getter
@Setter
public class Vertex {

    public static final int SIZEOF = (3 + 3 + 2) * Float.BYTES;
    public static final int OFFSETOF_POS = 0;
    public static final int OFFSETOF_COLOR = 3 * Float.BYTES;
    public static final int OFFSETOF_TEXTCOORDS = (3 + 3) * Float.BYTES;

    public Vertex(Vector3fc pos, Vector3fc color, Vector2fc texCoords) {
        this.pos = pos;
        this.color = color;
        this.texCoords = texCoords;
    }
    private Vector3fc pos;
    private Vector3fc color;
    private Vector2fc texCoords;
}
