package com.destrostudios.icetea.core;

import lombok.Getter;
import lombok.Setter;
import org.joml.Matrix4f;

@Getter
@Setter
public class UniformBufferObject {

    public static final int SIZEOF = 3 * 16 * Float.BYTES;

    public UniformBufferObject() {
        model = new Matrix4f();
        view = new Matrix4f();
        proj = new Matrix4f();
    }
    private Matrix4f model;
    private Matrix4f view;
    private Matrix4f proj;
}