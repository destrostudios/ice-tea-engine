package com.destrostudios.icetea.core.buffer;

import com.destrostudios.icetea.core.clone.CloneContext;

import static org.lwjgl.util.vma.Vma.*;

public class MemoryDataBuffer extends FieldsDataBuffer<ResizableMemoryBuffer> {

    public MemoryDataBuffer(int bufferUsage, boolean aligned) {
        super(new ResizableMemoryBuffer(bufferUsage, VMA_MEMORY_USAGE_AUTO_PREFER_DEVICE, VMA_ALLOCATION_CREATE_HOST_ACCESS_SEQUENTIAL_WRITE_BIT | VMA_ALLOCATION_CREATE_MAPPED_BIT), aligned);
    }

    public MemoryDataBuffer(MemoryDataBuffer memoryDataBufferResource, CloneContext context) {
        super(memoryDataBufferResource, context);
    }

    @Override
    public MemoryDataBuffer clone(CloneContext context) {
        return new MemoryDataBuffer(this, context);
    }
}