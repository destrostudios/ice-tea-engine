package com.destrostudios.icetea.core.compute;

import com.destrostudios.icetea.core.lifecycle.LifecycleObject;
import com.destrostudios.icetea.core.util.MathUtil;
import lombok.Getter;
import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.*;

import java.nio.IntBuffer;
import java.nio.LongBuffer;
import java.util.List;

import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.system.MemoryUtil.memAllocInt;
import static org.lwjgl.vulkan.VK10.*;

public abstract class ComputeJob extends LifecycleObject {

    private List<ComputeActionGroup> computeActionGroups;
    private VkCommandBuffer commandBuffer;
    @Getter
    private long signalSemaphore;
    private long fence;
    private Long waitSemaphore;
    private Integer waitDstStage;

    @Override
    protected void init() {
        super.init();
        computeActionGroups = createComputeActionGroups();
        for (ComputeActionGroup computeActionGroup : computeActionGroups) {
            computeActionGroup.update(application, 0);
        }
        initCommandBuffer();
        initFence();
    }

    protected abstract List<ComputeActionGroup> createComputeActionGroups();

    private void initCommandBuffer() {
        try (MemoryStack stack = stackPush()) {
            VkCommandBufferAllocateInfo commandBufferAllocateInfo = VkCommandBufferAllocateInfo.calloc()
                    .sType(VK_STRUCTURE_TYPE_COMMAND_BUFFER_ALLOCATE_INFO)
                    .commandPool(application.getCommandPool())
                    .level(VK_COMMAND_BUFFER_LEVEL_PRIMARY)
                    .commandBufferCount(1);

            PointerBuffer pCommandBuffers = stack.mallocPointer(1);
            int result = vkAllocateCommandBuffers(application.getLogicalDevice(), commandBufferAllocateInfo, pCommandBuffers);
            if (result != VK_SUCCESS) {
                throw new RuntimeException("Failed to allocate command buffers (result = " + result + ")");
            }
            commandBuffer = new VkCommandBuffer(pCommandBuffers.get(0), application.getLogicalDevice());

            VkCommandBufferBeginInfo bufferBeginInfo = VkCommandBufferBeginInfo.callocStack(stack);
            bufferBeginInfo.sType(VK_STRUCTURE_TYPE_COMMAND_BUFFER_BEGIN_INFO);
            bufferBeginInfo.flags(VK_COMMAND_BUFFER_USAGE_RENDER_PASS_CONTINUE_BIT);
            result = vkBeginCommandBuffer(commandBuffer, bufferBeginInfo);
            if (result != VK_SUCCESS) {
                throw new RuntimeException("Failed to begin recording command buffer (result = " + result + ")");
            }
            for (ComputeActionGroup computeActionGroup : computeActionGroups) {
                computeActionGroup.record(commandBuffer);
            }
            result = vkEndCommandBuffer(commandBuffer);
            if (result != VK_SUCCESS) {
                throw new RuntimeException("Failed to record command buffer (result = " + result + ")");
            }
        }
    }

    private void initFence() {
        try (MemoryStack stack = stackPush()) {
            VkFenceCreateInfo fenceCreateInfo = VkFenceCreateInfo.callocStack(stack);
            fenceCreateInfo.sType(VK_STRUCTURE_TYPE_FENCE_CREATE_INFO);
            fenceCreateInfo.flags(VK_FENCE_CREATE_SIGNALED_BIT);

            LongBuffer pFence = stack.mallocLong(1);
            int result = vkCreateFence(application.getLogicalDevice(), fenceCreateInfo, null, pFence);
            if (result != VK_SUCCESS) {
                throw new RuntimeException("Failed to create fence (result = " + result + ")");
            }
            fence = pFence.get(0);
        }
    }

    public void setWait(long waitSemaphore, int waitDstStage) {
        this.waitSemaphore = waitSemaphore;
        this.waitDstStage = waitDstStage;
    }

    public void submit() {
        try (MemoryStack stack = stackPush()) {
            VkSubmitInfo submitInfo = VkSubmitInfo.callocStack(stack);
            submitInfo.sType(VK_STRUCTURE_TYPE_SUBMIT_INFO);
            submitInfo.pCommandBuffers(stack.pointers(commandBuffer));

            if (shouldCreateSignalSemaphore()) {
                VkSemaphoreCreateInfo semaphoreCreateInfo = VkSemaphoreCreateInfo.callocStack(stack);
                semaphoreCreateInfo.sType(VK_STRUCTURE_TYPE_SEMAPHORE_CREATE_INFO);
                LongBuffer pSignalSemaphore = stack.mallocLong(1);
                int result = vkCreateSemaphore(application.getLogicalDevice(), semaphoreCreateInfo, null, pSignalSemaphore);
                if (result != VK_SUCCESS) {
                    throw new RuntimeException("Failed to create signal semaphore (result = " + result + ")");
                }
                signalSemaphore = pSignalSemaphore.get(0);
                submitInfo.pSignalSemaphores(stack.longs(signalSemaphore));
            }

            if (waitSemaphore != null) {
                submitInfo.waitSemaphoreCount(1);
                submitInfo.pWaitSemaphores(stack.longs(waitSemaphore));
                IntBuffer pWaitDstStageMask = memAllocInt(1);
                pWaitDstStageMask.put(0, waitDstStage);
                submitInfo.pWaitDstStageMask(pWaitDstStageMask);
            }

            vkResetFences(application.getLogicalDevice(), fence);
            // TODO: This should actually use a queue that supports computing (which usually is the case with the graphics queue, but should still be checked)
            int result = vkQueueSubmit(application.getGraphicsQueue(), submitInfo, fence);
            if (result != VK_SUCCESS) {
                vkResetFences(application.getLogicalDevice(), fence);
                throw new RuntimeException("Failed to submit compute command buffer (result = " + result + ")");
            }
            VK10.vkWaitForFences(application.getLogicalDevice(), fence, true, MathUtil.UINT64_MAX);
        }
    }

    protected boolean shouldCreateSignalSemaphore() {
        return false;
    }

    @Override
    protected void cleanupInternal() {
        vkDestroyFence(application.getLogicalDevice(), fence, null);
        vkFreeCommandBuffers(application.getLogicalDevice(), application.getCommandPool(), commandBuffer);
        for (ComputeActionGroup computeActionGroup : computeActionGroups) {
            computeActionGroup.cleanup();
        }
        super.cleanupInternal();
    }
}
