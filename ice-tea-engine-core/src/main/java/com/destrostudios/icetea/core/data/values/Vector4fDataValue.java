package com.destrostudios.icetea.core.data.values;

import com.destrostudios.icetea.core.clone.CloneContext;
import org.joml.Vector4f;

import java.nio.ByteBuffer;

import static org.lwjgl.vulkan.VK10.VK_FORMAT_R32G32B32A32_SFLOAT;

public class Vector4fDataValue extends DataValue<Vector4f> {

    public Vector4fDataValue() {
        super(new Vector4f());
    }

    public Vector4fDataValue(Vector4fDataValue vector4fDataValue) {
        this();
        setValue(vector4fDataValue.value);
    }

    @Override
    public void setValue(Vector4f value) {
        this.value.set(value);
    }

    @Override
    public boolean hasEqualValue(Vector4f value) {
        return this.value.equals(value);
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
