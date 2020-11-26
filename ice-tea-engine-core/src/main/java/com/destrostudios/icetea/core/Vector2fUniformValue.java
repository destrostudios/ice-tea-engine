package com.destrostudios.icetea.core;

import org.joml.Vector2f;

import java.nio.ByteBuffer;

public class Vector2fUniformValue extends UniformValue<Vector2f> {

    @Override
    public int getSize() {
        return 2 * Float.BYTES;
    }

    @Override
    public void write(ByteBuffer buffer, int index) {
        value.get(index, buffer);
    }

    @Override
    public String getShaderDefinitionType() {
        return "vec2";
    }
}
