package com.destrostudios.icetea.core;

import lombok.Getter;
import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.util.vma.VmaAllocationCreateInfo;
import org.lwjgl.util.vma.VmaAllocationInfo;
import org.lwjgl.util.vma.VmaAllocatorCreateInfo;
import org.lwjgl.util.vma.VmaVulkanFunctions;
import org.lwjgl.vulkan.*;

import java.nio.ByteBuffer;
import java.nio.LongBuffer;
import java.util.function.Consumer;

import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.util.vma.Vma.*;
import static org.lwjgl.vulkan.VK10.*;

public class MemoryManager {

    public MemoryManager(Application application) {
        this.application = application;
    }
    private Application application;
    @Getter
    private long allocator;

    public void init() {
        try (MemoryStack stack = stackPush()) {
            VmaVulkanFunctions vulkanFunctions = VmaVulkanFunctions.calloc(stack);
            vulkanFunctions.set(application.getInstance(), application.getLogicalDevice());

            VmaAllocatorCreateInfo allocatorCreateInfo = VmaAllocatorCreateInfo.calloc(stack);
            allocatorCreateInfo.physicalDevice(application.getPhysicalDevice());
            allocatorCreateInfo.device(application.getLogicalDevice());
            allocatorCreateInfo.pVulkanFunctions(vulkanFunctions);
            allocatorCreateInfo.instance(application.getInstance());

            PointerBuffer pAllocator = stack.mallocPointer(1);
            int result = vmaCreateAllocator(allocatorCreateInfo, pAllocator);
            if (result != VK_SUCCESS) {
                throw new RuntimeException("Failed to create allocator (result = " + result + ")");
            }
            allocator = pAllocator.get(0);
        }
    }

    public void createBuffer(long size, int bufferUsage, int memoryUsage, int memoryFlags, LongBuffer pBuffer, PointerBuffer pBufferAllocation) {
        try (MemoryStack stack = stackPush()) {
            VkBufferCreateInfo bufferCreateInfo = VkBufferCreateInfo.calloc(stack);
            bufferCreateInfo.sType(VK_STRUCTURE_TYPE_BUFFER_CREATE_INFO);
            bufferCreateInfo.size(size);
            bufferCreateInfo.usage(bufferUsage);
            bufferCreateInfo.sharingMode(VK_SHARING_MODE_EXCLUSIVE);

            VmaAllocationCreateInfo allocationCreateInfo = VmaAllocationCreateInfo.calloc(stack);
            allocationCreateInfo.flags(memoryFlags);
            allocationCreateInfo.usage(memoryUsage);

            int result = vmaCreateBuffer(allocator, bufferCreateInfo, allocationCreateInfo, pBuffer, pBufferAllocation, null);
            if (result != VK_SUCCESS) {
                throw new RuntimeException("Failed to create buffer (result = " + result + ")");
            }
        }
    }

    public void writeBuffer(long bufferAllocation, long bufferSize, Consumer<ByteBuffer> write) {
        try (MemoryStack stack = stackPush()) {
            PointerBuffer dataPointer = stack.mallocPointer(1);
            int result = vmaMapMemory(allocator, bufferAllocation, dataPointer);
            if (result != VK_SUCCESS) {
                throw new RuntimeException("Failed to map memory (result = " + result + ")");
            }
            ByteBuffer byteBuffer = dataPointer.getByteBuffer(0, (int) bufferSize);
            write.accept(byteBuffer);
            vmaUnmapMemory(allocator, bufferAllocation);
        }
    }

    public void copyBuffer(long sourceBuffer, long destinationBuffer, long size) {
        try (MemoryStack stack = stackPush()) {
            VkBufferCopy.Buffer region = VkBufferCopy.calloc(1, stack);
            region.size(size);
            application.getCommandPool().executeSingleTimeCommands(commandBuffer -> {
                vkCmdCopyBuffer(commandBuffer, sourceBuffer, destinationBuffer, region);
            });
        }
    }

    public void destroyBuffer(long buffer, long allocation) {
        vmaDestroyBuffer(allocator, buffer, allocation);
    }

    public void cleanup() {
        vmaDestroyAllocator(allocator);
    }
}
