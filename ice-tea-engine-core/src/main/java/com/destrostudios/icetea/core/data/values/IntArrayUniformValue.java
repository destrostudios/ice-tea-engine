package com.destrostudios.icetea.core.data.values;

import com.destrostudios.icetea.core.clone.CloneContext;

import java.nio.ByteBuffer;

public class IntArrayUniformValue extends UniformValue<int[]> {

    public IntArrayUniformValue() { }

    public IntArrayUniformValue(IntArrayUniformValue intArrayUniformValue) {
        value = new int[intArrayUniformValue.value.length];
        System.arraycopy(intArrayUniformValue.value, 0, value, 0, intArrayUniformValue.value.length);
    }

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

    @Override
    public IntArrayUniformValue clone(CloneContext context) {
        return new IntArrayUniformValue(this);
    }
}
