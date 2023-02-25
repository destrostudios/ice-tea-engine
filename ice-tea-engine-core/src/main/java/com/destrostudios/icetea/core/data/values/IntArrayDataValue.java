package com.destrostudios.icetea.core.data.values;

import com.destrostudios.icetea.core.clone.CloneContext;

import java.nio.ByteBuffer;

public class IntArrayDataValue extends DataValue<int[]> {

    public IntArrayDataValue() { }

    public IntArrayDataValue(IntArrayDataValue intArrayDataValue) {
        value = new int[intArrayDataValue.value.length];
        System.arraycopy(intArrayDataValue.value, 0, value, 0, intArrayDataValue.value.length);
    }

    @Override
    public int getSize() {
        return value.length * Integer.BYTES;
    }

    @Override
    public int getAlignedSize() {
        return value.length * 4 * Integer.BYTES;
    }

    @Override
    public void write(ByteBuffer buffer, int index, boolean aligned) {
        int currentIndex = index;
        for (int integer : value) {
            buffer.putInt(currentIndex, integer);
            currentIndex += Integer.BYTES;
            // FIXME: Gross overkill: Fill up to 16 bytes (Uniform blocks require array elements to be aligned to 16 bytes - It should be a vec4 array with 4 actual elements in each vec4 instead)
            if (aligned) {
                for (int i = 0; i < 3; i++) {
                    buffer.putInt(currentIndex, 0);
                    currentIndex += Integer.BYTES;
                }
            }
        }
    }

    @Override
    public String getShaderDefinitionType() {
        return "int[" + value.length + "]";
    }

    @Override
    public int getFormat() {
        throw new UnsupportedOperationException();
    }

    @Override
    public IntArrayDataValue clone(CloneContext context) {
        return new IntArrayDataValue(this);
    }
}
