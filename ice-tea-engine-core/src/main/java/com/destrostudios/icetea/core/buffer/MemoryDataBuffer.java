package com.destrostudios.icetea.core.buffer;

import com.destrostudios.icetea.core.clone.CloneContext;

import static org.lwjgl.vulkan.VK10.VK_MEMORY_PROPERTY_HOST_COHERENT_BIT;
import static org.lwjgl.vulkan.VK10.VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT;

public class MemoryDataBuffer extends FieldsDataBuffer<ResizableMemoryBuffer> {

    public MemoryDataBuffer(int usage) {
        super(new ResizableMemoryBuffer(usage, VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT | VK_MEMORY_PROPERTY_HOST_COHERENT_BIT));
    }

    public MemoryDataBuffer(MemoryDataBuffer memoryDataBufferResource, CloneContext context) {
        super(memoryDataBufferResource, context);
    }

    @Override
    public MemoryDataBuffer clone(CloneContext context) {
        return new MemoryDataBuffer(this, context);
    }
}