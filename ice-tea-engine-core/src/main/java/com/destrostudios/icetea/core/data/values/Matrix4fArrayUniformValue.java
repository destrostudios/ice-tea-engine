package com.destrostudios.icetea.core.data.values;

import com.destrostudios.icetea.core.clone.CloneContext;
import org.joml.Matrix4f;

import java.nio.ByteBuffer;

public class Matrix4fArrayUniformValue extends UniformValue<Matrix4f[]> {

    public Matrix4fArrayUniformValue() { }

    public Matrix4fArrayUniformValue(Matrix4fArrayUniformValue matrix4fArrayUniformValue) {
        value = new Matrix4f[matrix4fArrayUniformValue.value.length];
        for (int i = 0; i < value.length; i++) {
            value[i] = new Matrix4f(matrix4fArrayUniformValue.value[i]);
        }
    }

    @Override
    public int getSize() {
        return value.length * 16 * Float.BYTES;
    }

    @Override
    public void write(ByteBuffer buffer, int index) {
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
    public Matrix4fArrayUniformValue clone(CloneContext context) {
        return new Matrix4fArrayUniformValue(this);
    }
}
