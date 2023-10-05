package com.destrostudios.icetea.core.buffer;

import com.destrostudios.icetea.core.clone.CloneContext;
import lombok.Getter;
import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.util.vma.VmaAllocationInfo;

import java.nio.ByteBuffer;
import java.nio.LongBuffer;
import java.util.function.Consumer;

import static org.lwjgl.system.MemoryStack.stackPush;

public class ResizableMemoryBuffer extends ResizableBuffer {

    public ResizableMemoryBuffer(ResizableMemoryBuffer resizableMemoryBuffer) {
        this(resizableMemoryBuffer.bufferUsage, resizableMemoryBuffer.memoryUsage, resizableMemoryBuffer.memoryFlags);
    }

    public ResizableMemoryBuffer(int bufferUsage, int memoryUsage, int memoryFlags) {
        this.bufferUsage = bufferUsage;
        this.memoryUsage = memoryUsage;
        this.memoryFlags = memoryFlags;
    }
    private int bufferUsage;
    private int memoryUsage;
    private int memoryFlags;
    @Getter
    protected Long buffer;
    protected Long allocation;
    protected VmaAllocationInfo allocationInfo;

    @Override
    protected void createBuffer() {
        try (MemoryStack stack = stackPush()) {
            LongBuffer pBuffer = stack.mallocLong(1);
            PointerBuffer pAllocation = stack.mallocPointer(1);
            allocationInfo = VmaAllocationInfo.callocStack(stack);
            application.getMemoryManager().createBuffer(size, bufferUsage, memoryUsage, memoryFlags, pBuffer, pAllocation, allocationInfo);
            buffer = pBuffer.get(0);
            allocation = pAllocation.get(0);
        }
    }

    @Override
    protected void write(Consumer<ByteBuffer> write) {
        application.getMemoryManager().writeBuffer(allocation, size, write);
    }

    @Override
    protected void cleanupBuffer() {
        if (buffer != null) {
            application.getMemoryManager().destroyBuffer(buffer, allocation);
            buffer = null;
            allocation = null;
        }
    }

    @Override
    public ResizableMemoryBuffer clone(CloneContext context) {
        return new ResizableMemoryBuffer(this);
    }
}
