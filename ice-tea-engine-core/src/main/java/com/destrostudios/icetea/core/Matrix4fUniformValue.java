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
        value.get(index, buffer);
    }

    @Override
    public String getShaderDefinitionType() {
        return "mat4";
    }

    @Override
    public int getFormat() {
        throw new UnsupportedOperationException();
    }
}
