package com.destrostudios.icetea.core.buffer;

import com.destrostudios.icetea.core.clone.CloneContext;
import lombok.Getter;

import java.nio.ByteBuffer;

import static org.lwjgl.system.MemoryUtil.memAlloc;

public class ByteDataBuffer extends DataBuffer {

    public ByteDataBuffer() { }

    public ByteDataBuffer(ByteDataBuffer byteDataBuffer, CloneContext context) {
        super(byteDataBuffer, context);
    }
    @Getter
    private ByteBuffer byteBuffer;

    @Override
    protected void initBufferInternal() {
        byteBuffer = memAlloc(data.getSize());
    }

    @Override
    protected ByteBuffer prepareUpdatingBuffer() {
        return byteBuffer;
    }

    @Override
    protected void finishUpdatingBuffer() {
        // Nothing to do here
    }

    @Override
    protected void cleanupBuffer() {
        // Nothing to do here
        byteBuffer = null;
    }

    @Override
    public ByteDataBuffer clone(CloneContext context) {
        return new ByteDataBuffer(this, context);
    }
}