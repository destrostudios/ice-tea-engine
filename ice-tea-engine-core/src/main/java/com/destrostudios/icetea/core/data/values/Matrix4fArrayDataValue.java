package com.destrostudios.icetea.core.data.values;

import com.destrostudios.icetea.core.clone.CloneContext;
import org.joml.Matrix4f;

import java.nio.ByteBuffer;

public class Matrix4fArrayDataValue extends DataValue<Matrix4f[]> {

    public Matrix4fArrayDataValue() { }

    public Matrix4fArrayDataValue(Matrix4fArrayDataValue matrix4fArrayDataValue) {
        value = new Matrix4f[matrix4fArrayDataValue.value.length];
        for (int i = 0; i < value.length; i++) {
            value[i] = new Matrix4f(matrix4fArrayDataValue.value[i]);
        }
    }

    @Override
    public int getSize() {
        return value.length * 16 * Float.BYTES;
    }

    @Override
    public void write(ByteBuffer buffer, int index, boolean aligned) {
        int currentIndex = index;
        for (Matrix4f matrix4f : value) {
            matrix4f.get(currentIndex, buffer);
            currentIndex += 16 * Float.BYTES;
        }
    }

    @Override
    public String getShaderDefinitionType() {
        return "mat4[" + value.length + "]";
    }

    @Override
    public int getFormat() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Matrix4fArrayDataValue clone(CloneContext context) {
        return new Matrix4fArrayDataValue(this);
    }
}
