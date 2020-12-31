package com.destrostudios.icetea.core.data;

import static org.lwjgl.vulkan.VK10.VK_BUFFER_USAGE_UNIFORM_BUFFER_BIT;

public class UniformData extends MemoryBufferData {

    public UniformData() {
        super(VK_BUFFER_USAGE_UNIFORM_BUFFER_BIT);
    }
}