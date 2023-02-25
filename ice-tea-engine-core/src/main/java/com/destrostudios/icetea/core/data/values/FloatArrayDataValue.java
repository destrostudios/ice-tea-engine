package com.destrostudios.icetea.core.data.values;

import com.destrostudios.icetea.core.clone.CloneContext;

import java.nio.ByteBuffer;

public class FloatArrayDataValue extends DataValue<float[]> {

    public FloatArrayDataValue() { }

    public FloatArrayDataValue(FloatArrayDataValue floatArrayDataValue) {
        value = new float[floatArrayDataValue.value.length];
        System.arraycopy(floatArrayDataValue.value, 0, value, 0, floatArrayDataValue.value.length);
    }

    @Override
    public int getSize() {
        return value.length * Float.BYTES;
    }

    @Override
    public int getAlignedSize() {
        return value.length * 4 * Float.BYTES;
    }

    @Override
    public void write(ByteBuffer buffer, int index, boolean aligned) {
        int currentIndex = index;
        for (float floatValue : value) {
            buffer.putFloat(currentIndex, floatValue);
            currentIndex += Float.BYTES;
            // FIXME: Gross overkill: Fill up to 16 bytes (Uniform blocks require array elements to be aligned to 16 bytes - It should be a vec4 array with 4 actual elements in each vec4 instead)
            if (aligned) {
                for (int i = 0; i < 3; i++) {
                    buffer.putFloat(currentIndex, 0);
                    currentIndex += Float.BYTES;
                }
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
    public FloatArrayDataValue clone(CloneContext context) {
        return new FloatArrayDataValue(this);
    }
}
