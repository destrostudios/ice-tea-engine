package com.destrostudios.icetea.core.model;

import lombok.Getter;
import lombok.Setter;
import org.joml.Vector2f;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class ObjModel {

    public ObjModel() {
        this.positions = new ArrayList<>();
        this.texCoords = new ArrayList<>();
        this.indices = new ArrayList<>();
        this.normals = new ArrayList<>();
    }
    private List<Vector3f> positions;
    private List<Vector2f> texCoords;
    private List<Integer> indices;
    private List<Vector3f> normals;
}
