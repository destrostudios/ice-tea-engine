package com.destrostudios.icetea.core.data.values;

import com.destrostudios.icetea.core.clone.CloneContext;

import java.nio.ByteBuffer;

public class BooleanUniformValue extends UniformValue<Boolean> {

    public BooleanUniformValue() { }

    public BooleanUniformValue(BooleanUniformValue booleanUniformValue) {
        value = booleanUniformValue.getValue();
    }

    @Override
    public int getSize() {
        return Integer.BYTES;
    }

    @Override
    public void write(ByteBuffer buffer, int index) {
        buffer.putInt(index, value ? 1 : 0);
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
    public BooleanUniformValue clone(CloneContext context) {
        return new BooleanUniformValue(this);
    }
}
