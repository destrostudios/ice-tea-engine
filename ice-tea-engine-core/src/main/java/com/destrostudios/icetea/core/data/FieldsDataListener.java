package com.destrostudios.icetea.core.data;

import com.destrostudios.icetea.core.data.values.UniformValue;

public interface FieldsDataListener {

    void onFieldValueAdded(UniformValue<?> uniformValue);

    void onFieldValueSet(UniformValue<?> uniformValue);

    void onFieldValueRemoved(UniformValue<?> uniformValue);
}