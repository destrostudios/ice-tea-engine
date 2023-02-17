package com.destrostudios.icetea.core.data;

import com.destrostudios.icetea.core.clone.CloneContext;
import com.destrostudios.icetea.core.clone.ContextCloneable;
import com.destrostudios.icetea.core.data.values.*;
import com.destrostudios.icetea.core.lifecycle.LifecycleObject;
import lombok.Getter;
import lombok.Setter;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector4f;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

public class FieldsData extends LifecycleObject implements ContextCloneable {

    public FieldsData(Function<UniformValue<?>, Integer> getValueSize) {
        this.getValueSize = getValueSize;
    }

    public FieldsData(FieldsData fieldsData, CloneContext context) {
        for (Map.Entry<String, UniformValue<?>> entry : fieldsData.fields.entrySet()) {
            fields.put(entry.getKey(), entry.getValue().clone(context));
        }
        getValueSize = fieldsData.getValueSize;
        size = fieldsData.size;
    }
    @Getter
    protected Map<String, UniformValue<?>> fields = new LinkedHashMap<>();
    private Function<UniformValue<?>, Integer> getValueSize;
    @Getter
    protected int size;
    @Setter
    private FieldsDataListener listener;

    public void setInt(String name, Integer value) {
        set(name, value, IntUniformValue::new);
    }

    public void setIntArray(String name, int[] value) {
        set(name, value, IntArrayUniformValue::new);
    }

    public void setFloat(String name, Float value) {
        set(name, value, FloatUniformValue::new);
    }

    public void setVector2f(String name, Vector2f value) {
        set(name, value, Vector2fUniformValue::new);
    }

    public void setVector3f(String name, Vector3f value) {
        set(name, value, Vector3fUniformValue::new);
    }

    public void setVector4f(String name, Vector4f value) {
        set(name, value, Vector4fUniformValue::new);
    }

    public void setMatrix4f(String name, Matrix4f value) {
        set(name, value, Matrix4fUniformValue::new);
    }

    public void setMatrix4fArray(String name, Matrix4f[] value) {
        set(name, value, Matrix4fArrayUniformValue::new);
    }

    private <T> void set(String name, T value, Supplier<UniformValue<T>> uniformValueSupplier) {
        UniformValue<T> uniformValue = (UniformValue<T>) fields.get(name);
        if (uniformValue == null) {
            uniformValue = uniformValueSupplier.get();
            uniformValue.setValue(value);
            fields.put(name, uniformValue);
            size += getValueSize.apply(uniformValue);
        } else {
            uniformValue.setValue(value);
        }
        if (listener != null) {
            listener.onFieldsDataModified(uniformValue);
        }
    }

    public void clear(String name) {
        UniformValue<?> uniformValue = fields.remove(name);
        size -= getValueSize.apply(uniformValue);
        if (listener != null) {
            listener.onFieldsDataModified(uniformValue);
        }
    }

    public Integer getInt(String name) {
        return get(name);
    }

    public int[] getIntArray(String name) {
        return get(name);
    }

    public Float getFloat(String name) {
        return get(name);
    }

    public Vector2f getVector2f(String name) {
        return get(name);
    }

    public Vector3f getVector3f(String name) {
        return get(name);
    }

    public Vector4f getVector4f(String name) {
        return get(name);
    }

    public Matrix4f getMatrix4f(String name) {
        return get(name);
    }

    public Matrix4f[] getMatrix4fArray(String name) {
        return get(name);
    }

    private <T> T get(String name) {
        UniformValue<T> uniformValue = (UniformValue<T>) fields.get(name);
        return ((uniformValue != null) ? uniformValue.getValue() : null);
    }

    @Override
    public FieldsData clone(CloneContext context) {
        return new FieldsData(this, context);
    }
}