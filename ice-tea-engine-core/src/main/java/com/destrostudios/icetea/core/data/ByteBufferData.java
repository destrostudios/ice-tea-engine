package com.destrostudios.icetea.core.data;

import lombok.Getter;
import org.lwjgl.system.MemoryStack;

import java.nio.ByteBuffer;
import java.util.ArrayList;

import static org.lwjgl.system.MemoryUtil.memAlloc;

public class ByteBufferData extends BufferData {

    @Getter
    private ArrayList<ByteBuffer> byteBuffers;

    @Override
    protected void initBuffersInternal(int buffersCount, MemoryStack stack) {
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
}