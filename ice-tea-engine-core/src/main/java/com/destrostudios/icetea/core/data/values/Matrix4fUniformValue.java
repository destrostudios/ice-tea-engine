package com.destrostudios.icetea.core.data.values;

import com.destrostudios.icetea.core.clone.CloneContext;
import org.joml.Matrix4f;

import java.nio.ByteBuffer;

public class Matrix4fUniformValue extends UniformValue<Matrix4f> {

    public Matrix4fUniformValue() { }

    public Matrix4fUniformValue(Matrix4fUniformValue matrix4fUniformValue) {
        value = new Matrix4f(matrix4fUniformValue.value);
    }

    @Override
    public int getSize() {
        return 16 * Float.BYTES;
    }

    @Override
    public void write(ByteBuffer buffer, int index) {
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
    public Matrix4fUniformValue clone(CloneContext context) {
        return new Matrix4fUniformValue(this);
    }
}
