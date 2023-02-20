package com.destrostudios.icetea.core.data.values;

import com.destrostudios.icetea.core.clone.CloneContext;

import java.nio.ByteBuffer;

public class FloatArrayUniformValue extends UniformValue<float[]> {

    public FloatArrayUniformValue() { }

    public FloatArrayUniformValue(FloatArrayUniformValue floatArrayUniformValue) {
        value = new float[floatArrayUniformValue.value.length];
        System.arraycopy(floatArrayUniformValue.value, 0, value, 0, floatArrayUniformValue.value.length);
    }

    @Override
    public int getSize() {
        // See FIXME comment below about the 16 bytes alignment
        return value.length * 4 * Float.BYTES;
    }

    @Override
    public void write(ByteBuffer buffer, int index) {
        int currentIndex = index;
        for (float floatValue : value) {
            buffer.putFloat(currentIndex, floatValue);
            currentIndex += Float.BYTES;
            // FIXME: Gross overkill: Fill up to 16 bytes (Uniform blocks require array elements to be aligned to 16 bytes - It should be a vec4 array with 4 actual elements in each vec4 instead)
            for (int i = 0; i < 3; i++) {
                buffer.putFloat(currentIndex, 0);
                currentIndex += Float.BYTES;
            }
        }
    }

    @Override
    public String getShaderDefinitionType() {
        return "float[" + value.length + "]";
    }

    @Override
    public int getFormat() {
        throw new UnsupportedOperationException();
    }

    @Override
    public FloatArrayUniformValue clone(CloneContext context) {
        return new FloatArrayUniformValue(this);
    }
}
