package com.destrostudios.icetea.core;

import lombok.Getter;
import lombok.Setter;

import java.nio.ByteBuffer;

public abstract class UniformValue<T> {

    @Getter
    @Setter
    protected T value;

    public int getAlignedSize() {
        return getSize();
    }

    public abstract int getSize();

    public abstract void write(ByteBuffer buffer, int offset);

    public abstract String getShaderDefinitionType();

    public abstract int getFormat();

}
