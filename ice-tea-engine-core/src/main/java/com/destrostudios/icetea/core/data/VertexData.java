package com.destrostudios.icetea.core.data;

import com.destrostudios.icetea.core.data.values.UniformValue;

public class VertexData extends FieldsData {

    @Override
    protected int getSize(UniformValue<?> uniformValue) {
        return uniformValue.getSize();
    }
}
