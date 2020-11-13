package com.destrostudios.icetea.core;

import java.nio.ByteBuffer;

public class FloatUniformValue extends UniformValue<Float> {

    @Override
    public int getSize() {
        return Float.BYTES;
    }

    @Override
    public void write(ByteBuffer buffer, int index) {
        buffer.putFloat(index, value);
    }
}
