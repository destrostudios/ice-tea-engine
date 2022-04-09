package com.destrostudios.icetea.core.data;

import com.destrostudios.icetea.core.clone.CloneContext;

import static org.lwjgl.vulkan.VK10.VK_BUFFER_USAGE_UNIFORM_BUFFER_BIT;

public class UniformData extends MemoryBufferData {

    public UniformData() {
        super(VK_BUFFER_USAGE_UNIFORM_BUFFER_BIT);
    }

    public UniformData(UniformData uniformData, CloneContext context) {
        super(uniformData, context);
    }

    @Override
    public UniformData clone(CloneContext context) {
        return new UniformData(this, context);
    }
}