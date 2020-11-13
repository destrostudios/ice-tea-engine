package com.destrostudios.icetea.core;

import org.joml.Vector4f;

import java.nio.ByteBuffer;

public class Vector4fUniformValue extends UniformValue<Vector4f> {

    @Override
    public int getSize() {
        return 4 * Float.BYTES;
    }

    @Override
    public void write(ByteBuffer buffer, int index) {
        value.get(index, buffer);
    }
}
