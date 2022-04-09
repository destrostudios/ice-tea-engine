package com.destrostudios.icetea.core.data;

import com.destrostudios.icetea.core.clone.CloneContext;

import static org.lwjgl.vulkan.VK10.VK_BUFFER_USAGE_STORAGE_BUFFER_BIT;

public class StorageBufferData extends MemoryBufferData {

    public StorageBufferData() {
        super(VK_BUFFER_USAGE_STORAGE_BUFFER_BIT);
    }

    public StorageBufferData(StorageBufferData storageBufferData, CloneContext context) {
        super(storageBufferData, context);
    }

    @Override
    public StorageBufferData clone(CloneContext context) {
        return new StorageBufferData(this, context);
    }
}