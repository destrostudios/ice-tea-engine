package com.destrostudios.icetea.core.buffer;

import com.destrostudios.icetea.core.clone.CloneContext;

public class ByteDataBuffer extends FieldsDataBuffer<ResizableByteBuffer> {

    public ByteDataBuffer(boolean aligned) {
        super(new ResizableByteBuffer(), aligned);
    }

    public ByteDataBuffer(ByteDataBuffer byteDataBuffer, CloneContext context) {
        super(byteDataBuffer, context);
    }

    @Override
    public ByteDataBuffer clone(CloneContext context) {
        return new ByteDataBuffer(this, context);
    }
}