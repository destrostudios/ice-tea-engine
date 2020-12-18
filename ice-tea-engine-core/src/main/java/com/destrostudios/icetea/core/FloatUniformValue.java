package com.destrostudios.icetea.core;

import java.nio.ByteBuffer;

import static org.lwjgl.vulkan.VK10.VK_FORMAT_R32_SFLOAT;

public class FloatUniformValue extends UniformValue<Float> {

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
}
