package com.destrostudios.icetea.core.data.values;

import com.destrostudios.icetea.core.clone.CloneContext;
import org.joml.Matrix4f;

import java.nio.ByteBuffer;

public class Matrix4fDataValue extends DataValue<Matrix4f> {

    public Matrix4fDataValue() {
        super(new Matrix4f());
    }

    public Matrix4fDataValue(Matrix4fDataValue matrix4fDataValue) {
        this();
        setValue(matrix4fDataValue.value);
    }

    @Override
    public void setValue(Matrix4f value) {
        this.value.set(value);
    }

    @Override
    public boolean hasEqualValue(Matrix4f value) {
        return this.value.equals(value);
    }

    @Override
    public int getSize() {
        return 16 * Float.BYTES;
    }

    @Override
    public void write(ByteBuffer buffer, int index, boolean aligned) {
        value.get(index, buffer);
    }

    @Override
    public String getShaderDefinitionType() {
        return "mat4";
    }

    @Override
    public int getFormat() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Matrix4fDataValue clone(CloneContext context) {
        return new Matrix4fDataValue(this);
    }
}
