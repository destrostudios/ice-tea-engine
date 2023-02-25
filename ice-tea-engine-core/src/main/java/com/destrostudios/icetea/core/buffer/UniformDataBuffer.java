package com.destrostudios.icetea.core.buffer;

import com.destrostudios.icetea.core.clone.CloneContext;

import static org.lwjgl.vulkan.VK10.VK_BUFFER_USAGE_UNIFORM_BUFFER_BIT;

public class UniformDataBuffer extends MemoryDataBuffer {

    public UniformDataBuffer() {
        super(VK_BUFFER_USAGE_UNIFORM_BUFFER_BIT, true);
    }

    public UniformDataBuffer(UniformDataBuffer uniformDataBuffer, CloneContext context) {
        super(uniformDataBuffer, context);
    }

    @Override
    public UniformDataBuffer clone(CloneContext context) {
        return new UniformDataBuffer(this, context);
    }
}