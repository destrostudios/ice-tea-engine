package com.destrostudios.icetea.core;

import lombok.Getter;
import lombok.Setter;
import org.joml.Vector2fc;
import org.joml.Vector3fc;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class Model {

    public Model() {
        this.positions = new ArrayList<>();
        this.texCoords = new ArrayList<>();
        this.indices = new ArrayList<>();
        this.normals = new ArrayList<>();
    }
    private List<Vector3fc> positions;
    private List<Vector2fc> texCoords;
    private List<Integer> indices;
    private List<Vector3fc> normals;
}
