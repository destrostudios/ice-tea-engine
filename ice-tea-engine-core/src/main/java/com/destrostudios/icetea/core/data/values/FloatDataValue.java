package com.destrostudios.icetea.core.data.values;

import com.destrostudios.icetea.core.clone.CloneContext;

import java.nio.ByteBuffer;

import static org.lwjgl.vulkan.VK10.VK_FORMAT_R32_SFLOAT;

public class FloatDataValue extends DataValue<Float> {

    public FloatDataValue() {
        super(null);
    }

    public FloatDataValue(FloatDataValue floatDataValue) {
        this();
        setValue(floatDataValue.getValue());
    }

    @Override
    public void setValue(Float value) {
        this.value = value;
    }

    @Override
    public boolean hasEqualValue(Float value) {
        return this.value.equals(value);
    }

    @Override
    public int getSize() {
        return Float.BYTES;
    }

    @Override
    public void write(ByteBuffer buffer, int index, boolean aligned) {
        buffer.putFloat(index, value);
    }

    @Override
    public String getShaderDefinitionType() {
        return "float";
    }

    @Override
    public int getFormat() {
        return VK_FORMAT_R32_SFLOAT;
    }

    @Override
    public FloatDataValue clone(CloneContext context) {
        return new FloatDataValue(this);
    }
}
