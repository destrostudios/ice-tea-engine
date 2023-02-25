package com.destrostudios.icetea.core.buffer;

import com.destrostudios.icetea.core.clone.CloneContext;

public class PushConstantsDataBuffer extends ByteDataBuffer {

    public PushConstantsDataBuffer() {
        super(true);
    }

    public PushConstantsDataBuffer(PushConstantsDataBuffer pushConstantsDataBuffer, CloneContext context) {
        super(pushConstantsDataBuffer, context);
    }

    @Override
    public PushConstantsDataBuffer clone(CloneContext context) {
        return new PushConstantsDataBuffer(this, context);
    }
}