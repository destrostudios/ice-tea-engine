package com.destrostudios.icetea.core.data.values;

import com.destrostudios.icetea.core.clone.CloneContext;

import java.nio.ByteBuffer;

public class BooleanDataValue extends DataValue<Boolean> {

    public BooleanDataValue() {
        super(null);
    }

    public BooleanDataValue(BooleanDataValue booleanDataValue) {
        this();
        setValue(booleanDataValue.getValue());
    }

    @Override
    public void setValue(Boolean value) {
        this.value = value;
    }

    @Override
    public boolean hasEqualValue(Boolean value) {
        return this.value.equals(value);
    }

    @Override
    public int getSize() {
        return Integer.BYTES;
    }

    @Override
    public void write(ByteBuffer buffer, int index, boolean aligned) {
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
    public BooleanDataValue clone(CloneContext context) {
        return new BooleanDataValue(this);
    }
}
