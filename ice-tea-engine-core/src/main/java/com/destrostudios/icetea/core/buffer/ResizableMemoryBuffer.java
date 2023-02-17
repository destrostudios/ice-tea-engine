package com.destrostudios.icetea.core.buffer;

import com.destrostudios.icetea.core.clone.CloneContext;
import lombok.Getter;
import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;

import java.nio.ByteBuffer;
import java.nio.LongBuffer;
import java.util.function.Consumer;

import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.vulkan.VK10.*;
import static org.lwjgl.vulkan.VK10.vkFreeMemory;

public class ResizableMemoryBuffer extends ResizableBuffer {

    public ResizableMemoryBuffer(ResizableMemoryBuffer resizableMemoryBuffer) {
        this(resizableMemoryBuffer.usage, resizableMemoryBuffer.properties);
    }

    public ResizableMemoryBuffer(int usage, int properties) {
        this.usage = usage;
        this.properties = properties;
    }
    private int usage;
    private int properties;
    @Getter
    protected Long buffer;
    protected Long bufferMemory;

    @Override
    protected void createBuffer() {
        try (MemoryStack stack = stackPush()) {
            LongBuffer pBuffer = stack.mallocLong(1);
            LongBuffer pBufferMemory = stack.mallocLong(1);
            application.getBufferManager().createBuffer(size, usage, properties, pBuffer, pBufferMemory);
            buffer = pBuffer.get(0);
            bufferMemory = pBufferMemory.get(0);
        }
    }

    @Override
    protected void write(Consumer<ByteBuffer> write) {
        try (MemoryStack stack = stackPush()) {
            PointerBuffer dataPointer = stack.mallocPointer(1);
            int result = vkMapMemory(application.getLogicalDevice(), bufferMemory, 0, size, 0, dataPointer);
            if (result != VK_SUCCESS) {
                throw new RuntimeException("Failed to map memory (result = " + result + ")");
            }
            ByteBuffer byteBuffer = dataPointer.getByteBuffer(0, (int) size);
            write.accept(byteBuffer);
            vkUnmapMemory(application.getLogicalDevice(), bufferMemory);
        }
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
    public ResizableMemoryBuffer clone(CloneContext context) {
        return new ResizableMemoryBuffer(this);
    }
}
