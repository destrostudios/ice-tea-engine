package com.destrostudios.icetea.core.data.values;

import com.destrostudios.icetea.core.clone.CloneContext;
import org.joml.Vector4f;

import java.nio.ByteBuffer;

import static org.lwjgl.vulkan.VK10.VK_FORMAT_R32G32B32A32_SFLOAT;

public class Vector4fUniformValue extends UniformValue<Vector4f> {

    public Vector4fUniformValue() { }

    public Vector4fUniformValue(Vector4fUniformValue vector4fUniformValue) {
        value = new Vector4f(vector4fUniformValue.value);
    }

    @Override
    public int getSize() {
        return 4 * Float.BYTES;
    }

    @Override
    public void write(ByteBuffer buffer, int index) {
        value.get(index, buffer);
    }

    @Override
    public String getShaderDefinitionType() {
        return "vec4";
    }

    @Override
    public int getFormat() {
        return VK_FORMAT_R32G32B32A32_SFLOAT;
    }

    @Override
    public Vector4fUniformValue clone(CloneContext context) {
        return new Vector4fUniformValue(this);
    }
}
