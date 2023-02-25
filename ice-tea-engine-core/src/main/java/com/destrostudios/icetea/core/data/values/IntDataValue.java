package com.destrostudios.icetea.core.data.values;

import com.destrostudios.icetea.core.clone.CloneContext;

import java.nio.ByteBuffer;

public class IntDataValue extends DataValue<Integer> {

    public IntDataValue() { }

    public IntDataValue(IntDataValue intDataValue) {
        value = intDataValue.getValue();
    }

    @Override
    public int getSize() {
        return Integer.BYTES;
    }

    @Override
    public void write(ByteBuffer buffer, int index, boolean aligned) {
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
    public IntDataValue clone(CloneContext context) {
        return new IntDataValue(this);
    }
}
