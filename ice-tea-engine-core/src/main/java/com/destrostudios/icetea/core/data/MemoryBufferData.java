package com.destrostudios.icetea.core.data;

import lombok.Getter;
import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;

import java.nio.ByteBuffer;
import java.nio.LongBuffer;
import java.util.ArrayList;

import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.vulkan.VK10.*;

public class MemoryBufferData extends BufferData {

    public MemoryBufferData(int usage) {
        this.usage = usage;
    }
    private int usage;
    @Getter
    private ArrayList<Long> buffers;
    private ArrayList<Long> buffersMemory;

    @Override
    protected void initBuffersInternal(int buffersCount, MemoryStack stack) {
        buffers = new ArrayList<>(buffersCount);
        buffersMemory = new ArrayList<>(buffersCount);
        LongBuffer pBuffer = stack.mallocLong(1);
        LongBuffer pBufferMemory = stack.mallocLong(1);
        for (int i = 0; i < buffersCount; i++) {
            application.getBufferManager().createBuffer(
                size,
                usage,
                VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT | VK_MEMORY_PROPERTY_HOST_COHERENT_BIT,
                pBuffer,
                pBufferMemory
            );
            buffers.add(pBuffer.get(0));
            buffersMemory.add(pBufferMemory.get(0));
        }
    }

    @Override
    protected ByteBuffer prepareUpdatingBuffer(int bufferIndex) {
        try (MemoryStack stack = stackPush()) {
            PointerBuffer data = stack.mallocPointer(1);
            vkMapMemory(application.getLogicalDevice(), buffersMemory.get(bufferIndex), 0, size, 0, data);
            return data.getByteBuffer(0, size);
        }
    }

    @Override
    protected void finishUpdatingBuffer(int bufferIndex) {
        vkUnmapMemory(application.getLogicalDevice(), buffersMemory.get(bufferIndex));
    }

    @Override
    public void cleanupBuffer() {
        if (buffers != null) {
            for (long uniformBuffer : buffers) {
                vkDestroyBuffer(application.getLogicalDevice(), uniformBuffer, null);
            }
            buffers = null;
        }
        if (buffersMemory != null) {
            for (long uniformBufferMemory : buffersMemory) {
                vkFreeMemory(application.getLogicalDevice(), uniformBufferMemory, null);
            }
            buffersMemory = null;
        }
    }
}