package com.destrostudios.icetea.core.data.values;

import java.nio.ByteBuffer;

public class IntUniformValue extends UniformValue<Integer> {

    @Override
    public int getSize() {
        return Integer.BYTES;
    }

    @Override
    public void write(ByteBuffer buffer, int index) {
        buffer.putInt(index, value);
    }

    @Override
    public String getShaderDefinitionType() {
        return "int";
    }

    @Override
    public int getFormat() {
        throw new UnsupportedOperationException();
    }
}
