package com.destrostudios.icetea.core.data;

import com.destrostudios.icetea.core.clone.CloneContext;
import lombok.Getter;

import java.nio.ByteBuffer;

import static org.lwjgl.system.MemoryUtil.memAlloc;

public class ByteBufferData extends BufferData {

    public ByteBufferData() { }

    public ByteBufferData(ByteBufferData byteBufferData, CloneContext context) {
        super(byteBufferData, context);
    }
    @Getter
    private ByteBuffer byteBuffer;

    @Override
    protected void initBufferInternal() {
        byteBuffer = memAlloc(size);
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
    public ByteBufferData clone(CloneContext context) {
        return new ByteBufferData(this, context);
    }
}