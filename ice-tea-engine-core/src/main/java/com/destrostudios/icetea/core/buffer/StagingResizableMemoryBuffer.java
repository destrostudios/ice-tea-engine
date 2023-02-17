package com.destrostudios.icetea.core.buffer;


import static org.lwjgl.vulkan.VK10.*;

public class StagingResizableMemoryBuffer extends ResizableMemoryBuffer {

    public StagingResizableMemoryBuffer() {
        super(VK_BUFFER_USAGE_TRANSFER_SRC_BIT, VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT | VK_MEMORY_PROPERTY_HOST_COHERENT_BIT);
    }
}
