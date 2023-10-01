package com.destrostudios.icetea.core.command;

import com.destrostudios.icetea.core.object.NativeObject;
import lombok.Getter;
import org.lwjgl.vulkan.VkCommandBuffer;

import java.util.ArrayList;
import java.util.LinkedList;

import static org.lwjgl.vulkan.VK10.VK_COMMAND_BUFFER_LEVEL_SECONDARY;

public class SecondaryCommandBufferPool extends NativeObject {

    public SecondaryCommandBufferPool() {
        allCommandBuffers = new ArrayList<>();
        availableCommandBuffers = new LinkedList<>();
    }
    @Getter
    private CommandPool commandPool;
    private ArrayList<VkCommandBuffer> allCommandBuffers;
    private LinkedList<VkCommandBuffer> availableCommandBuffers;

    @Override
    protected void initNative() {
        super.initNative();
        commandPool = new CommandPool(application);
    }

    @Override
    protected void updateNative() {
        super.updateNative();
        commandPool.updateNative(application);
        availableCommandBuffers.clear();
        availableCommandBuffers.addAll(allCommandBuffers);
    }

    public VkCommandBuffer getOrAllocateCommandBuffer() {
        VkCommandBuffer commandBuffer = availableCommandBuffers.poll();
        if (commandBuffer == null) {
            commandBuffer = commandPool.allocateCommandBuffer(VK_COMMAND_BUFFER_LEVEL_SECONDARY);
            allCommandBuffers.add(commandBuffer);
        }
        return commandBuffer;
    }

    @Override
    protected void cleanupNativeInternal() {
        commandPool.freeCommandBuffers(allCommandBuffers);
        commandPool.cleanupNative();
        super.cleanupNativeInternal();
    }
}
