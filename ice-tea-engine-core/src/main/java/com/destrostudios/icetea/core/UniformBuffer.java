package com.destrostudios.icetea.core;

import lombok.Getter;
import lombok.Setter;
import org.joml.Matrix4f;

@Getter
@Setter
public class UniformBuffer {

    public static final int SIZEOF = 3 * 16 * Float.BYTES;

    private Matrix4f model;
    private Matrix4f view;
    private Matrix4f proj;
}