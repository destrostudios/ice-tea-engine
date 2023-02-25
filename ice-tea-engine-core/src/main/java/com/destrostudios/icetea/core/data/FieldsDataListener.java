package com.destrostudios.icetea.core.data;

import com.destrostudios.icetea.core.data.values.DataValue;

public interface FieldsDataListener {

    void onFieldsDataModified(DataValue<?> dataValue);
}