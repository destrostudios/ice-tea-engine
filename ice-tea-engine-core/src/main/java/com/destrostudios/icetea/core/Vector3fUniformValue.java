package com.destrostudios.icetea.core;

import org.joml.Vector3f;

import java.nio.ByteBuffer;

public class Vector3fUniformValue extends UniformValue<Vector3f> {

    @Override
    public int getSize() {
        return 4 * Float.BYTES;
    }

    @Override
    public void write(ByteBuffer buffer, int index) {
        value.get(index, buffer);
    }

    @Override
    public String getShaderDefinitionType() {
        return "vec3";
    }
}
