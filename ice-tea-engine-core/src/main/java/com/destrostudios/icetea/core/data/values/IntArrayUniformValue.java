package com.destrostudios.icetea.core.data.values;

import java.nio.ByteBuffer;

public class IntArrayUniformValue extends UniformValue<int[]> {

    @Override
    public int getSize() {
        return value.length * Integer.BYTES;
    }

    @Override
    public void write(ByteBuffer buffer, int index) {
        int currentIndex = index;
        for (int integer : value) {
            buffer.putInt(currentIndex, integer);
            currentIndex += Integer.BYTES;
        }
    }

    @Override
    public String getShaderDefinitionType() {
        return "int[]";
    }

    @Override
    public int getFormat() {
        throw new UnsupportedOperationException();
    }
}
