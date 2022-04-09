package com.destrostudios.icetea.core.data.values;

import com.destrostudios.icetea.core.clone.CloneContext;
import com.destrostudios.icetea.core.clone.ContextCloneable;
import lombok.Getter;
import lombok.Setter;

import java.nio.ByteBuffer;

public abstract class UniformValue<T> implements ContextCloneable {

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

    public abstract UniformValue<T> clone(CloneContext context);
}
