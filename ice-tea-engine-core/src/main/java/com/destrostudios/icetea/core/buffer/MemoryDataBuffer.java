package com.destrostudios.icetea.core.buffer;

import com.destrostudios.icetea.core.clone.CloneContext;
import lombok.Getter;
import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;

import java.nio.ByteBuffer;
import java.nio.LongBuffer;

import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.vulkan.VK10.*;

public class MemoryDataBuffer extends DataBuffer {

    public MemoryDataBuffer(int usage) {
        this.usage = usage;
    }

    public MemoryDataBuffer(MemoryDataBuffer memoryDataBuffer, CloneContext context) {
        super(memoryDataBuffer, context);
        this.usage = memoryDataBuffer.usage;
    }
    private int usage;
    @Getter
    private Long buffer;
    private Long bufferMemory;

    @Override
    protected void initBufferInternal() {
        try (MemoryStack stack = stackPush()) {
            LongBuffer pBuffer = stack.mallocLong(1);
            LongBuffer pBufferMemory = stack.mallocLong(1);
            application.getBufferManager().createBuffer(
                data.getSize(),
                usage,
                VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT | VK_MEMORY_PROPERTY_HOST_COHERENT_BIT,
                pBuffer,
                pBufferMemory
            );
            buffer = pBuffer.get(0);
            bufferMemory = pBufferMemory.get(0);
        }
    }

    @Override
    protected ByteBuffer prepareUpdatingBuffer() {
        try (MemoryStack stack = stackPush()) {
            PointerBuffer dataBuffer = stack.mallocPointer(1);
            vkMapMemory(application.getLogicalDevice(), bufferMemory, 0, data.getSize(), 0, dataBuffer);
            return dataBuffer.getByteBuffer(0, data.getSize());
        }
    }

    @Override
    protected void finishUpdatingBuffer() {
        vkUnmapMemory(application.getLogicalDevice(), bufferMemory);
    }

    @Override
    protected void cleanupBuffer() {
        if (buffer != null) {
            vkDestroyBuffer(application.getLogicalDevice(), buffer, null);
            buffer = null;
        }
        if (bufferMemory != null) {
            vkFreeMemory(application.getLogicalDevice(), bufferMemory, null);
            bufferMemory = null;
        }
    }

    @Override
    public MemoryDataBuffer clone(CloneContext context) {
        return new MemoryDataBuffer(this, context);
    }
}