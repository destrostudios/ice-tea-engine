package com.destrostudios.icetea.core;

import lombok.Getter;
import org.joml.Quaternionf;
import org.joml.Vector3f;

@Getter
public class Transform {

    public Transform() {
        translation = new Vector3f();
        quaternion = new Quaternionf();
        scale = new Vector3f(1, 1, 1);
    }
    private Vector3f translation;
    private Quaternionf quaternion;
    private Vector3f scale;
}
