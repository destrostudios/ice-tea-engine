package com.destrostudios.icetea.core.data.values;

import com.destrostudios.icetea.core.clone.CloneContext;

import java.nio.ByteBuffer;

public class IntDataValue extends DataValue<Integer> {

    public IntDataValue() {
        super(null);
    }

    public IntDataValue(IntDataValue intDataValue) {
        this();
        setValue(intDataValue.getValue());
    }

    @Override
    public void setValue(Integer value) {
        this.value = value;
    }

    @Override
    public boolean hasEqualValue(Integer value) {
        return this.value.equals(value);
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
