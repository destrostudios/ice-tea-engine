package com.destrostudios.icetea.core.data;

import com.destrostudios.icetea.core.data.values.UniformValue;

public interface FieldsDataListener {

    void onFieldsDataModified(UniformValue<?> uniformValue);
}