package com.destrostudios.icetea.core.data.values;

import com.destrostudios.icetea.core.clone.CloneContext;
import com.destrostudios.icetea.core.clone.ContextCloneable;
import lombok.Getter;

import java.nio.ByteBuffer;

public abstract class DataValue<T> implements ContextCloneable {

    protected DataValue(T initialValue) {
        value = initialValue;
    }
    @Getter
    protected T value;

    public abstract void setValue(T value);

    public abstract boolean hasEqualValue(T value);

    public int getAlignedSize() {
        return getSize();
    }

    public abstract int getSize();

    public abstract void write(ByteBuffer buffer, int offset, boolean aligned);

    public abstract String getShaderDefinitionType();

    public abstract int getFormat();

    public abstract DataValue<T> clone(CloneContext context);
}
