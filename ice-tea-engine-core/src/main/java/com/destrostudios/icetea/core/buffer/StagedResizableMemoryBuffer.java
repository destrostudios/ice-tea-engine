package com.destrostudios.icetea.core.buffer;

import java.nio.ByteBuffer;
import java.util.function.Consumer;

import static org.lwjgl.vulkan.VK10.*;

public class StagedResizableMemoryBuffer extends ResizableMemoryBuffer {

    public StagedResizableMemoryBuffer(int usage, int properties) {
        super(usage | VK_BUFFER_USAGE_TRANSFER_DST_BIT, properties);
        stagingBuffer = new StagingResizableMemoryBuffer();
    }
    private StagingResizableMemoryBuffer stagingBuffer;

    @Override
    public void updateNative() {
        super.updateNative();
        stagingBuffer.updateNative(application);
    }

    @Override
    protected boolean setSizeAndRecreateIfNecessary(long size) {
        super.setSizeAndRecreateIfNecessary(size);
        return stagingBuffer.setSizeAndRecreateIfNecessary(size);
    }

    @Override
    protected void write(Consumer<ByteBuffer> write) {
        stagingBuffer.write(write);
        application.getBufferManager().copyBuffer(stagingBuffer.getBuffer(), buffer, size);
    }

    @Override
    protected void cleanupNativeInternal() {
        stagingBuffer.cleanupNative();
        super.cleanupNativeInternal();
    }
}
