package com.destrostudios.icetea.core;

import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.*;

import java.nio.LongBuffer;

import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.vulkan.VK10.*;

public class BufferManager {

    public BufferManager(Application application) {
        this.application = application;
    }
    private Application application;

    public void createBuffer(long size, int usage, int properties, LongBuffer pBuffer, LongBuffer pBufferMemory) {
        try (MemoryStack stack = stackPush()) {
            VkBufferCreateInfo bufferCreateInfo = VkBufferCreateInfo.callocStack(stack);
            bufferCreateInfo.sType(VK_STRUCTURE_TYPE_BUFFER_CREATE_INFO);
            bufferCreateInfo.size(size);
            bufferCreateInfo.usage(usage);
            bufferCreateInfo.sharingMode(VK_SHARING_MODE_EXCLUSIVE);

            int result = vkCreateBuffer(application.getLogicalDevice(), bufferCreateInfo, null, pBuffer);
            if (result != VK_SUCCESS) {
                throw new RuntimeException("Failed to create buffer (result = " + result + ")");
            }

            VkMemoryAllocateInfo allocInfo = VkMemoryAllocateInfo.callocStack(stack);
            allocInfo.sType(VK_STRUCTURE_TYPE_MEMORY_ALLOCATE_INFO);
            VkMemoryRequirements memRequirements = VkMemoryRequirements.mallocStack(stack);
            vkGetBufferMemoryRequirements(application.getLogicalDevice(), pBuffer.get(0), memRequirements);
            allocInfo.allocationSize(memRequirements.size());
            allocInfo.memoryTypeIndex(application.findMemoryType(memRequirements.memoryTypeBits(), properties));

            result = vkAllocateMemory(application.getLogicalDevice(), allocInfo, null, pBufferMemory);
            if (result != VK_SUCCESS) {
                throw new RuntimeException("Failed to allocate buffer memory (result = " + result + ")");
            }
            vkBindBufferMemory(application.getLogicalDevice(), pBuffer.get(0), pBufferMemory.get(0), 0);
        }
    }

    public void copyBuffer(long sourceBuffer, long destinationBuffer, long size) {
        try (MemoryStack stack = stackPush()) {
            VkBufferCopy.Buffer copyRegion = VkBufferCopy.callocStack(1, stack);
            copyRegion.size(size);
            VkCommandBuffer commandBuffer = application.getCommandPool().beginSingleTimeCommands();
            vkCmdCopyBuffer(commandBuffer, sourceBuffer, destinationBuffer, copyRegion);
            application.getCommandPool().endSingleTimeCommands(commandBuffer);
        }
    }
}
