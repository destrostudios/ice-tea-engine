package com.destrostudios.icetea.core.data.values;

import com.destrostudios.icetea.core.clone.CloneContext;
import org.joml.Vector3f;

import java.nio.ByteBuffer;

import static org.lwjgl.vulkan.VK10.VK_FORMAT_R32G32B32_SFLOAT;

public class Vector3fDataValue extends DataValue<Vector3f> {

    public Vector3fDataValue() {
        super(new Vector3f());
    }

    public Vector3fDataValue(Vector3fDataValue vector3fDataValue) {
        this();
        setValue(vector3fDataValue.value);
    }

    @Override
    public void setValue(Vector3f value) {
        this.value.set(value);
    }

    @Override
    public boolean hasEqualValue(Vector3f value) {
        return this.value.equals(value);
    }

    @Override
    public int getSize() {
        return 3 * Float.BYTES;
    }

    @Override
    public int getAlignedSize() {
        return 4 * Float.BYTES;
    }

    @Override
    public void write(ByteBuffer buffer, int index, boolean aligned) {
        value.get(index, buffer);
        if (aligned) {
            buffer.putFloat(index + 3 * Float.BYTES, 0);
        }
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
    public Vector3fDataValue clone(CloneContext context) {
        return new Vector3fDataValue(this);
    }
}
