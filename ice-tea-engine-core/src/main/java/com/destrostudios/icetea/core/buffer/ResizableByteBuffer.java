package com.destrostudios.icetea.core.buffer;

import com.destrostudios.icetea.core.clone.CloneContext;
import lombok.Getter;

import java.nio.ByteBuffer;
import java.util.function.Consumer;

import static org.lwjgl.system.MemoryUtil.memAlloc;
import static org.lwjgl.system.MemoryUtil.memFree;

public class ResizableByteBuffer extends ResizableBuffer {

    public ResizableByteBuffer() { }

    public ResizableByteBuffer(ResizableByteBuffer resizableByteBuffer) {
        super(resizableByteBuffer);
    }
    @Getter
    private ByteBuffer byteBuffer;

    @Override
    protected void createBuffer() {
        byteBuffer = memAlloc((int) size);
    }

    @Override
    protected void write(Consumer<ByteBuffer> write) {
        write.accept(byteBuffer);
        // TODO: Check if we shouldn't rewind here or generally align rewind handling everywhere
    }

    @Override
    protected void cleanupBuffer() {
        if (byteBuffer != null) {
            memFree(byteBuffer);
            byteBuffer = null;
        }
    }

    @Override
    public ResizableByteBuffer clone(CloneContext context) {
        return new ResizableByteBuffer(this);
    }
}