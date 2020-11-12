package com.destrostudios.icetea.core;

import org.joml.Matrix4f;

import java.nio.ByteBuffer;

public class Matrix4fUniformValue extends UniformValue<Matrix4f> {

    @Override
    public int getSize() {
        return 16 * Float.BYTES;
    }

    @Override
    public void write(ByteBuffer buffer, int index) {
        value.get(AlignmentUtils.alignas(index, AlignmentUtils.alignof(value)), buffer);
    }
}
