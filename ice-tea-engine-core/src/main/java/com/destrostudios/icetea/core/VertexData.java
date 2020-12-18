package com.destrostudios.icetea.core;

public class VertexData extends FieldsData {

    @Override
    protected int getSize(UniformValue<?> uniformValue) {
        return uniformValue.getSize();
    }
}
