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

import java.nio.ByteBuffer;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;

public class FieldsData extends LifecycleObject implements ContextCloneable {

    public FieldsData(boolean aligned) {
        this.aligned = aligned;
    }

    public FieldsData(FieldsData fieldsData, CloneContext context) {
        for (Map.Entry<String, DataValue<?>> entry : fieldsData.fields.entrySet()) {
            fields.put(entry.getKey(), entry.getValue().clone(context));
        }
        aligned = fieldsData.aligned;
        size = fieldsData.size;
    }
    @Getter
    protected Map<String, DataValue<?>> fields = new LinkedHashMap<>();
    private boolean aligned;
    @Getter
    protected int size;
    @Setter
    private FieldsDataListener listener;

    public void setBoolean(String name, Boolean value) {
        set(name, value, BooleanDataValue::new);
    }

    public void setInt(String name, Integer value) {
        set(name, value, IntDataValue::new);
    }

    public void setIntArray(String name, int[] value) {
        set(name, value, IntArrayDataValue::new);
    }

    public void setFloat(String name, Float value) {
        set(name, value, FloatDataValue::new);
    }

    public void setFloatArray(String name, float[] value) {
        set(name, value, FloatArrayDataValue::new);
    }

    public void setVector2f(String name, Vector2f value) {
        set(name, value, Vector2fDataValue::new);
    }

    public void setVector3f(String name, Vector3f value) {
        set(name, value, Vector3fDataValue::new);
    }

    public void setVector4f(String name, Vector4f value) {
        set(name, value, Vector4fDataValue::new);
    }

    public void setMatrix4f(String name, Matrix4f value) {
        set(name, value, Matrix4fDataValue::new);
    }

    public void setMatrix4fArray(String name, Matrix4f[] value) {
        set(name, value, Matrix4fArrayDataValue::new);
    }

    private <T> void set(String name, T value, Supplier<DataValue<T>> dataValueSupplier) {
        DataValue<T> dataValue = (DataValue<T>) fields.get(name);
        if (dataValue == null) {
            dataValue = dataValueSupplier.get();
            dataValue.setValue(value);
            fields.put(name, dataValue);
            size += getSize(dataValue);
        } else {
            dataValue.setValue(value);
        }
        if (listener != null) {
            listener.onFieldsDataModified(dataValue);
        }
    }

    public void clear(String name) {
        DataValue<?> dataValue = fields.remove(name);
        size -= getSize(dataValue);
        if (listener != null) {
            listener.onFieldsDataModified(dataValue);
        }
    }

    public int getSize(DataValue<?> dataValue) {
        return (aligned ? dataValue.getAlignedSize() : dataValue.getSize());
    }

    public void write(ByteBuffer buffer, int offset, DataValue<?> dataValue) {
        dataValue.write(buffer, offset, aligned);
    }

    public Boolean getBoolean(String name) {
        return get(name);
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

    public float[] getFloatArray(String name) {
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
        DataValue<T> dataValue = (DataValue<T>) fields.get(name);
        return ((dataValue != null) ? dataValue.getValue() : null);
    }

    @Override
    public FieldsData clone(CloneContext context) {
        return new FieldsData(this, context);
    }
}