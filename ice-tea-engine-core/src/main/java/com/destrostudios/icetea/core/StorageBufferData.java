package com.destrostudios.icetea.core;

import static org.lwjgl.vulkan.VK10.VK_BUFFER_USAGE_STORAGE_BUFFER_BIT;

public class StorageBufferData extends MemoryBufferData {

    public StorageBufferData() {
        super(VK_BUFFER_USAGE_STORAGE_BUFFER_BIT);
    }
}