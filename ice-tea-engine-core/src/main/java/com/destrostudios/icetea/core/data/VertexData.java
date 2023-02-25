package com.destrostudios.icetea.core.data;

import com.destrostudios.icetea.core.clone.CloneContext;

public class VertexData extends FieldsData {

    public VertexData() {
        super(false);
    }

    public VertexData(VertexData vertexData, CloneContext context) {
        super(vertexData, context);
    }

    @Override
    public VertexData clone(CloneContext context) {
        return new VertexData(this, context);
    }
}
