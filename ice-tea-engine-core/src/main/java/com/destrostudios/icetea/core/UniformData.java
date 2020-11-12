package com.destrostudios.icetea.core;

import lombok.Getter;
import org.joml.Matrix4f;

import java.nio.ByteBuffer;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;

public class UniformData {

    public UniformData() {
        fields = new LinkedHashMap<>();
    }
    private Map<String, UniformValue<?>> fields;
    @Getter
    private int size;

    public void setMatrix4f(String name, Matrix4f value) {
        set(name, value, Matrix4fUniformValue::new);
    }

    private <T> void set(String name, T value, Supplier<UniformValue<T>> uniformDataSupplier) {
        UniformValue<T> uniformValue = (UniformValue<T>) fields.get(name);
        if (uniformValue == null) {
            uniformValue = uniformDataSupplier.get();
            fields.put(name, uniformValue);
            size += uniformValue.getSize();
        }
        uniformValue.setValue(value);
    }

    public void clear(String name) {
        UniformValue<?> value = fields.remove(name);
        size -= value.getSize();
    }

    public Matrix4f getMatrix4f(String name) {
        return get(name);
    }

    private <T> T get(String name) {
        UniformValue<T> uniformValue = (UniformValue<T>) fields.get(name);
        return ((uniformValue != null) ? uniformValue.getValue() : null);
    }

    public void write(ByteBuffer byteBuffer) {
        int index = 0;
        for (UniformValue<?> value : fields.values()) {
            value.write(byteBuffer, index);
            index += value.getSize();
        }
    }
}