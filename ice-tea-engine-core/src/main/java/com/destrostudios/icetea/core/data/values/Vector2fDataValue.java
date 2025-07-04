package com.destrostudios.icetea.core.data.values;

import com.destrostudios.icetea.core.clone.CloneContext;
import org.joml.Vector2f;

import java.nio.ByteBuffer;

import static org.lwjgl.vulkan.VK10.VK_FORMAT_R32G32_SFLOAT;

public class Vector2fDataValue extends DataValue<Vector2f> {

    public Vector2fDataValue() {
        super(new Vector2f());
    }

    public Vector2fDataValue(Vector2fDataValue vector2fDataValue) {
        this();
        setValue(vector2fDataValue.value);
    }

    @Override
    public void setValue(Vector2f value) {
        this.value.set(value);
    }

    @Override
    public boolean hasEqualValue(Vector2f value) {
        return this.value.equals(value);
    }

    @Override
    public int getSize() {
        return 2 * Float.BYTES;
    }

    @Override
    public void write(ByteBuffer buffer, int index, boolean aligned) {
        value.get(index, buffer);
    }

    @Override
    public String getShaderDefinitionType() {
        return "vec2";
    }

    @Override
    public int getFormat() {
        return VK_FORMAT_R32G32_SFLOAT;
    }

    @Override
    public Vector2fDataValue clone(CloneContext context) {
        return new Vector2fDataValue(this);
    }
}
