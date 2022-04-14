package com.destrostudios.icetea.core.data;

import com.destrostudios.icetea.core.clone.CloneContext;
import lombok.Getter;
import org.lwjgl.system.MemoryStack;

import java.nio.ByteBuffer;
import java.util.ArrayList;

import static org.lwjgl.system.MemoryUtil.memAlloc;

public class ByteBufferData extends BufferData {

    public ByteBufferData() { }

    public ByteBufferData(ByteBufferData byteBufferData, CloneContext context) {
        super(byteBufferData, context);
    }
    @Getter
    private ArrayList<ByteBuffer> byteBuffers;

    @Override
    protected void initBuffersInternal(int buffersCount) {
        byteBuffers = new ArrayList<>(buffersCount);
        for (int i = 0; i < buffersCount; i++) {
            ByteBuffer byteBuffer = memAlloc(size);
            byteBuffers.add(byteBuffer);
        }
    }

    @Override
    protected ByteBuffer prepareUpdatingBuffer(int bufferIndex) {
        return byteBuffers.get(bufferIndex);
    }

    @Override
    protected void finishUpdatingBuffer(int bufferIndex) {
        // Nothing to do here
    }

    @Override
    public void cleanupBuffer() {
        if (byteBuffers != null) {
            // Nothing to do here
            byteBuffers = null;
        }
    }

    @Override
    public ByteBufferData clone(CloneContext context) {
        return new ByteBufferData(this, context);
    }
}