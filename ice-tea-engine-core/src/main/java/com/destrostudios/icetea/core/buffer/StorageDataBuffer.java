package com.destrostudios.icetea.core.buffer;

import com.destrostudios.icetea.core.clone.CloneContext;

import static org.lwjgl.vulkan.VK10.VK_BUFFER_USAGE_STORAGE_BUFFER_BIT;

public class StorageDataBuffer extends MemoryDataBuffer {

    public StorageDataBuffer() {
        super(VK_BUFFER_USAGE_STORAGE_BUFFER_BIT);
    }

    public StorageDataBuffer(StorageDataBuffer storageBufferData, CloneContext context) {
        super(storageBufferData, context);
    }

    @Override
    public StorageDataBuffer clone(CloneContext context) {
        return new StorageDataBuffer(this, context);
    }
}