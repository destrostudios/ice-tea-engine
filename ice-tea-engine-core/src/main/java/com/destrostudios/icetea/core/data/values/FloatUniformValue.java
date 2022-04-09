package com.destrostudios.icetea.core.data.values;

import com.destrostudios.icetea.core.clone.CloneContext;

import java.nio.ByteBuffer;

import static org.lwjgl.vulkan.VK10.VK_FORMAT_R32_SFLOAT;

public class FloatUniformValue extends UniformValue<Float> {

    public FloatUniformValue() { }

    public FloatUniformValue(FloatUniformValue floatUniformValue) {
        value = floatUniformValue.getValue();
    }

    @Override
    public int getSize() {
        return Float.BYTES;
    }

    @Override
    public void write(ByteBuffer buffer, int index) {
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
    public FloatUniformValue clone(CloneContext context) {
        return new FloatUniformValue(this);
    }
}
