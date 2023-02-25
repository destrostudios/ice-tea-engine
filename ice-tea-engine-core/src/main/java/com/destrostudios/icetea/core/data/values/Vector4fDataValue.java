package com.destrostudios.icetea.core.data.values;

import com.destrostudios.icetea.core.clone.CloneContext;
import org.joml.Vector4f;

import java.nio.ByteBuffer;

import static org.lwjgl.vulkan.VK10.VK_FORMAT_R32G32B32A32_SFLOAT;

public class Vector4fDataValue extends DataValue<Vector4f> {

    public Vector4fDataValue() { }

    public Vector4fDataValue(Vector4fDataValue vector4fDataValue) {
        value = new Vector4f(vector4fDataValue.value);
    }

    @Override
    public int getSize() {
        return 4 * Float.BYTES;
    }

    @Override
    public void write(ByteBuffer buffer, int index, boolean aligned) {
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
    public Vector4fDataValue clone(CloneContext context) {
        return new Vector4fDataValue(this);
    }
}
