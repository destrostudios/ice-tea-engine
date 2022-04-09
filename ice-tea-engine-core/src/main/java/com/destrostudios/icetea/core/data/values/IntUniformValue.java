package com.destrostudios.icetea.core.data.values;

import com.destrostudios.icetea.core.clone.CloneContext;

import java.nio.ByteBuffer;

public class IntUniformValue extends UniformValue<Integer> {

    public IntUniformValue() { }

    public IntUniformValue(IntUniformValue intUniformValue) {
        value = intUniformValue.getValue();
    }

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

    @Override
    public IntUniformValue clone(CloneContext context) {
        return new IntUniformValue(this);
    }
}
