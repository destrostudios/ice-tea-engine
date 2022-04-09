package com.destrostudios.icetea.core.data;

import com.destrostudios.icetea.core.clone.CloneContext;
import com.destrostudios.icetea.core.data.values.UniformValue;

public class VertexData extends FieldsData {

    public VertexData() { }

    public VertexData(VertexData vertexData, CloneContext context) {
        super(vertexData, context);
    }

    @Override
    protected int getSize(UniformValue<?> uniformValue) {
        return uniformValue.getSize();
    }

    @Override
    public VertexData clone(CloneContext context) {
        return new VertexData(this, context);
    }
}
