package com.destrostudios.icetea.core.data.values;

import com.destrostudios.icetea.core.clone.CloneContext;
import org.joml.Vector3f;

import java.nio.ByteBuffer;

import static org.lwjgl.vulkan.VK10.VK_FORMAT_R32G32B32_SFLOAT;

public class Vector3fUniformValue extends UniformValue<Vector3f> {

    public Vector3fUniformValue() { }

    public Vector3fUniformValue(Vector3fUniformValue vector3fUniformValue) {
        value = new Vector3f(vector3fUniformValue.value);
    }

    @Override
    public int getAlignedSize() {
        return 4 * Float.BYTES;
    }

    @Override
    public int getSize() {
        return 3 * Float.BYTES;
    }

    @Override
    public void write(ByteBuffer buffer, int index) {
        value.get(index, buffer);
    }

    @Override
    public String getShaderDefinitionType() {
        return "vec3";
    }

    @Override
    public int getFormat() {
        return VK_FORMAT_R32G32B32_SFLOAT;
    }

    @Override
    public Vector3fUniformValue clone(CloneContext context) {
        return new Vector3fUniformValue(this);
    }
}
