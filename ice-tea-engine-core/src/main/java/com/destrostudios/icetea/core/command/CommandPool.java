package com.destrostudios.icetea.core.command;

import com.destrostudios.icetea.core.Application;
import com.destrostudios.icetea.core.object.NativeObject;
import com.destrostudios.icetea.core.util.BufferUtil;
import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VkCommandBuffer;
import org.lwjgl.vulkan.VkCommandBufferAllocateInfo;
import org.lwjgl.vulkan.VkCommandPoolCreateInfo;

import java.nio.LongBuffer;
import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.vulkan.VK10.*;
import static org.lwjgl.vulkan.VK10.VK_SUCCESS;

public class CommandPool extends NativeObject {

    public CommandPool(Application application) {
        this.application = application;
    }
    private Application application;
    private Long commandPool;

    @Override
    protected void initNative() {
        super.initNative();
        initCommandPool();
    }

    public void initCommandPool() {
        try (MemoryStack stack = stackPush()) {
            VkCommandPoolCreateInfo poolCreateInfo = VkCommandPoolCreateInfo.callocStack(stack);
            poolCreateInfo.sType(VK_STRUCTURE_TYPE_COMMAND_POOL_CREATE_INFO);
            poolCreateInfo.flags(VK_COMMAND_POOL_CREATE_RESET_COMMAND_BUFFER_BIT);
            poolCreateInfo.queueFamilyIndex(application.getPhysicalDeviceInformation().getQueueFamilyIndexGraphics());

            LongBuffer pCommandPool = stack.mallocLong(1);
            int result = vkCreateCommandPool(application.getLogicalDevice(), poolCreateInfo, null, pCommandPool);
            if (result != VK_SUCCESS) {
                throw new RuntimeException("Failed to create command pool (result = " + result + ")");
            }
            commandPool = pCommandPool.get(0);
        }
    }

    public VkCommandBuffer allocateCommandBuffer(int level) {
        return allocateCommandBuffers(level, 1).get(0);
    }

    public ArrayList<VkCommandBuffer> allocateCommandBuffers(int level, int count) {
        try (MemoryStack stack = stackPush()) {
            VkCommandBufferAllocateInfo allocateInfo = VkCommandBufferAllocateInfo.callocStack(stack);
            allocateInfo.sType(VK_STRUCTURE_TYPE_COMMAND_BUFFER_ALLOCATE_INFO);
            allocateInfo.commandPool(commandPool);
            allocateInfo.level(level);
            allocateInfo.commandBufferCount(count);

            PointerBuffer pCommandBuffers = stack.mallocPointer(count);
            int result = vkAllocateCommandBuffers(application.getLogicalDevice(), allocateInfo, pCommandBuffers);
            if (result != VK_SUCCESS) {
                throw new RuntimeException("Failed to allocate command buffers (result = " + result + ")");
            }

            ArrayList<VkCommandBuffer> commandBuffers = new ArrayList<>(count);
            for (int i = 0; i < count; i++) {
                commandBuffers.add(new VkCommandBuffer(pCommandBuffers.get(i), application.getLogicalDevice()));
            }
            return commandBuffers;
        }
    }

    public void freeCommandBuffer(VkCommandBuffer commandBuffer) {
        vkFreeCommandBuffers(application.getLogicalDevice(), commandPool, commandBuffer);
    }

    public void freeCommandBuffers(List<VkCommandBuffer> commandBuffers) {
        try (MemoryStack stack = stackPush()) {
            vkFreeCommandBuffers(application.getLogicalDevice(), commandPool, BufferUtil.asPointerBuffer(commandBuffers, stack));
        }
    }

    @Override
    protected void cleanupNativeInternal() {
        vkDestroyCommandPool(application.getLogicalDevice(), commandPool, null);
        super.cleanupNativeInternal();
    }
}
