package com.destrostudios.icetea.core.data.values;

import org.joml.Matrix4f;

import java.nio.ByteBuffer;

public class Matrix4fArrayUniformValue extends UniformValue<Matrix4f[]> {

    @Override
    public int getSize() {
        return value.length * 16 * Integer.BYTES;
    }

    @Override
    public void write(ByteBuffer buffer, int index) {
        int currentIndex = index;
        for (Matrix4f matrix4f : value) {
            matrix4f.get(currentIndex, buffer);
            currentIndex += 16 * Integer.BYTES;
        }
    }

    @Override
    public String getShaderDefinitionType() {
        // TODO: Clarify if this is fine and called again when the array is changed/resized - It's needed to access it by index inside shaders. After clarifying, do the same result for IntArrayUniformValue
        return "mat4[" + value.length + "]";
    }

    @Override
    public int getFormat() {
        throw new UnsupportedOperationException();
    }
}
