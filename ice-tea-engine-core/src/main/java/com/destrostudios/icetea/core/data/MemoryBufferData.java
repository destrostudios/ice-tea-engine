package com.destrostudios.icetea.core.data;

import com.destrostudios.icetea.core.clone.CloneContext;
import lombok.Getter;
import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;

import java.nio.ByteBuffer;
import java.nio.LongBuffer;

import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.vulkan.VK10.*;

public class MemoryBufferData extends BufferData {

    public MemoryBufferData(int usage) {
        this.usage = usage;
    }

    public MemoryBufferData(MemoryBufferData memoryBufferData, CloneContext context) {
        super(memoryBufferData, context);
        this.usage = memoryBufferData.usage;
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
                size,
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
            PointerBuffer data = stack.mallocPointer(1);
            vkMapMemory(application.getLogicalDevice(), bufferMemory, 0, size, 0, data);
            return data.getByteBuffer(0, size);
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
    public MemoryBufferData clone(CloneContext context) {
        return new MemoryBufferData(this, context);
    }
}