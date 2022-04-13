package com.destrostudios.icetea.core;

import com.destrostudios.icetea.core.render.GeometryRenderContext;
import com.destrostudios.icetea.core.render.RenderJob;
import com.destrostudios.icetea.core.render.RenderJobManager;
import com.destrostudios.icetea.core.util.BufferUtil;
import com.destrostudios.icetea.core.util.MathUtil;
import lombok.Getter;
import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.*;

import java.nio.IntBuffer;
import java.nio.LongBuffer;
import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.glfw.GLFW.glfwGetFramebufferSize;
import static org.lwjgl.glfw.GLFW.glfwWaitEvents;
import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.vulkan.KHRSurface.*;
import static org.lwjgl.vulkan.KHRSurface.VK_PRESENT_MODE_FIFO_KHR;
import static org.lwjgl.vulkan.KHRSwapchain.*;
import static org.lwjgl.vulkan.KHRSwapchain.vkGetSwapchainImagesKHR;
import static org.lwjgl.vulkan.VK10.*;

public class SwapChain {

    public SwapChain() {
        renderJobManager = new RenderJobManager();
    }
    private Application application;
    @Getter
    private VkExtent2D extent;
    @Getter
    private long swapChain;
    @Getter
    private List<Long> images;
    @Getter
    private int imageFormat;
    @Getter
    private List<Long> imageViews;
    @Getter
    private List<VkCommandBuffer> commandBuffers;
    @Getter
    private RenderJobManager renderJobManager;

    public void recreate() {
        // Wait if minimized
        try (MemoryStack stack = stackPush()) {
            IntBuffer width = stack.ints(0);
            IntBuffer height = stack.ints(0);
            while ((width.get(0) == 0) && (height.get(0) == 0)) {
                glfwGetFramebufferSize(application.getWindow(), width, height);
                glfwWaitEvents();
            }
        }
        vkDeviceWaitIdle(application.getLogicalDevice());
        cleanup();
        init(application);
        recreateCommandBuffers();
    }

    public void init(Application application) {
        this.application = application;
        initSwapChain();
        initImageViews();
        initRenderJobs();
    }

    private void initSwapChain() {
        try (MemoryStack stack = stackPush()) {
            PhysicalDeviceInformation physicalDeviceInformation = application.getPhysicalDeviceInformation();
            VkSurfaceCapabilitiesKHR surfaceCapabilities = application.getPhysicalDeviceManager().getSurfaceCapabilities(stack);

            VkExtent2D extent = chooseSwapExtent(surfaceCapabilities, application.getWindow(), stack);
            this.extent = VkExtent2D.create().set(extent);

            VkSwapchainCreateInfoKHR swapchainCreateInfo = VkSwapchainCreateInfoKHR.callocStack(stack);
            swapchainCreateInfo.sType(VK_STRUCTURE_TYPE_SWAPCHAIN_CREATE_INFO_KHR);
            swapchainCreateInfo.surface(application.getSurface());

            IntBuffer imageCount = stack.ints(surfaceCapabilities.minImageCount() + 1);
            if ((surfaceCapabilities.maxImageCount() > 0) && (imageCount.get(0) > surfaceCapabilities.maxImageCount())) {
                imageCount.put(0, surfaceCapabilities.maxImageCount());
            }
            swapchainCreateInfo.minImageCount(imageCount.get(0));
            VkSurfaceFormatKHR surfaceFormat = chooseSwapSurfaceFormat(physicalDeviceInformation.getSurfaceFormats());
            swapchainCreateInfo.imageFormat(surfaceFormat.format());
            swapchainCreateInfo.imageColorSpace(surfaceFormat.colorSpace());
            swapchainCreateInfo.imageExtent(extent);
            swapchainCreateInfo.imageArrayLayers(1);
            swapchainCreateInfo.imageUsage(VK_IMAGE_USAGE_COLOR_ATTACHMENT_BIT);

            if (physicalDeviceInformation.getQueueFamilyIndexGraphics() != physicalDeviceInformation.getQueueFamilyIndexSurface()) {
                swapchainCreateInfo.imageSharingMode(VK_SHARING_MODE_CONCURRENT);
                swapchainCreateInfo.pQueueFamilyIndices(stack.ints(physicalDeviceInformation.getQueueFamilyIndexGraphics(), physicalDeviceInformation.getQueueFamilyIndexSurface()));
            } else {
                swapchainCreateInfo.imageSharingMode(VK_SHARING_MODE_EXCLUSIVE);
            }

            swapchainCreateInfo.preTransform(surfaceCapabilities.currentTransform());
            swapchainCreateInfo.compositeAlpha(VK_COMPOSITE_ALPHA_OPAQUE_BIT_KHR);
            int presentMode = chooseSwapPresentMode(physicalDeviceInformation.getSurfacePresentModes(), application.getConfig().getPreferredPresentMode());
            swapchainCreateInfo.presentMode(presentMode);
            swapchainCreateInfo.clipped(true);
            swapchainCreateInfo.oldSwapchain(VK_NULL_HANDLE);

            LongBuffer pSwapChain = stack.longs(VK_NULL_HANDLE);
            int result = vkCreateSwapchainKHR(application.getLogicalDevice(), swapchainCreateInfo, null, pSwapChain);
            if (result != VK_SUCCESS) {
                throw new RuntimeException("Failed to create swap chain (result = " + result + ")");
            }
            swapChain = pSwapChain.get(0);

            vkGetSwapchainImagesKHR(application.getLogicalDevice(), swapChain, imageCount, null);
            LongBuffer pImages = stack.mallocLong(imageCount.get(0));
            vkGetSwapchainImagesKHR(application.getLogicalDevice(), swapChain, imageCount, pImages);
            images = new ArrayList<>(imageCount.get(0));
            for (int i = 0;i < pImages.capacity();i++) {
                images.add(pImages.get(i));
            }
            imageFormat = surfaceFormat.format();
        }
    }

    private VkExtent2D chooseSwapExtent(VkSurfaceCapabilitiesKHR capabilities, long window, MemoryStack stack) {
        if (capabilities.currentExtent().width() != MathUtil.UINT32_MAX) {
            return capabilities.currentExtent();
        }
        IntBuffer width = stack.ints(0);
        IntBuffer height = stack.ints(0);
        glfwGetFramebufferSize(window, width, height);
        VkExtent2D actualExtent = VkExtent2D.mallocStack(stack).set(width.get(0), height.get(0));

        VkExtent2D minExtent = capabilities.minImageExtent();
        VkExtent2D maxExtent = capabilities.maxImageExtent();

        actualExtent.width(MathUtil.clamp(minExtent.width(), maxExtent.width(), actualExtent.width()));
        actualExtent.height(MathUtil.clamp(minExtent.height(), maxExtent.height(), actualExtent.height()));

        return actualExtent;
    }

    private VkSurfaceFormatKHR chooseSwapSurfaceFormat(VkSurfaceFormatKHR.Buffer availableFormats) {
        return availableFormats.stream()
                .filter(availableFormat -> availableFormat.format() == VK_FORMAT_B8G8R8_SRGB)
                .filter(availableFormat -> availableFormat.colorSpace() == VK_COLOR_SPACE_SRGB_NONLINEAR_KHR)
                .findAny()
                .orElse(availableFormats.get(0));
    }

    private int chooseSwapPresentMode(IntBuffer availablePresentModes, int preferredPresentMode) {
        for (int i = 0; i < availablePresentModes.capacity(); i++) {
            int presentMode = availablePresentModes.get(i);
            if (presentMode == preferredPresentMode) {
                return presentMode;
            }
        }
        return VK_PRESENT_MODE_FIFO_KHR;
    }

    private void initImageViews() {
        imageViews = new ArrayList<>(images.size());
        for (long swapChainImage : images) {
            imageViews.add(application.getImageManager().createImageView(swapChainImage, imageFormat, VK_IMAGE_ASPECT_COLOR_BIT, 1));
        }
    }

    public void recreateRenderJobs() {
        cleanupRenderJobs();
        initRenderJobs();
    }

    public void initRenderJobs() {
        // Make sure all render jobs are initialized first, because they can have dependencies between each other
        renderJobManager.forEachRenderJob(renderJob -> renderJob.init(application));
        renderJobManager.forEachRenderJob(renderJob -> {
            application.getRootNode().forEachGeometry(geometry -> {
                GeometryRenderContext<?> renderContext = geometry.getRenderContext(renderJob);
                if (renderContext != null) {
                    renderContext.createDescriptorDependencies();
                }
            });
        });
    }

    public void recreateCommandBuffers() {
        cleanupCommandBuffers();

        int commandBuffersCount = images.size();
        commandBuffers = new ArrayList<>(commandBuffersCount);
        try (MemoryStack stack = stackPush()) {
            VkCommandBufferAllocateInfo allocInfo = VkCommandBufferAllocateInfo.callocStack(stack);
            allocInfo.sType(VK_STRUCTURE_TYPE_COMMAND_BUFFER_ALLOCATE_INFO);
            allocInfo.commandPool(application.getCommandPool());
            allocInfo.level(VK_COMMAND_BUFFER_LEVEL_PRIMARY);
            allocInfo.commandBufferCount(commandBuffersCount);

            PointerBuffer pCommandBuffers = stack.mallocPointer(commandBuffersCount);
            int result = vkAllocateCommandBuffers(application.getLogicalDevice(), allocInfo, pCommandBuffers);
            if (result != VK_SUCCESS) {
                throw new RuntimeException("Failed to allocate command buffers (result = " + result + ")");
            }

            for (int i = 0; i < commandBuffersCount; i++) {
                commandBuffers.add(new VkCommandBuffer(pCommandBuffers.get(i), application.getLogicalDevice()));
            }

            VkCommandBufferBeginInfo bufferBeginInfo = VkCommandBufferBeginInfo.callocStack(stack);
            bufferBeginInfo.sType(VK_STRUCTURE_TYPE_COMMAND_BUFFER_BEGIN_INFO);

            VkRenderPassBeginInfo renderPassBeginInfo = VkRenderPassBeginInfo.callocStack(stack);
            for(int i = 0; i < commandBuffersCount;i++) {
                VkCommandBuffer commandBuffer = commandBuffers.get(i);
                result = vkBeginCommandBuffer(commandBuffer, bufferBeginInfo);
                if (result != VK_SUCCESS) {
                    throw new RuntimeException("Failed to begin recording command buffer (result = " + result + ")");
                }
                render(renderJobManager.getQueuePreScene(), commandBuffer, i, renderPassBeginInfo, stack);
                render(renderJobManager.getSceneRenderJob(), commandBuffer, i, renderPassBeginInfo, stack);
                render(renderJobManager.getQueuePostScene(), commandBuffer, i, renderPassBeginInfo, stack);
                result = vkEndCommandBuffer(commandBuffer);
                if (result != VK_SUCCESS) {
                    throw new RuntimeException("Failed to record command buffer (result = " + result + ")");
                }
            }
        }
    }

    private void render(List<RenderJob<?>> renderJobBucket, VkCommandBuffer commandBuffer, int commandBufferIndex, VkRenderPassBeginInfo renderPassBeginInfo, MemoryStack stack) {
        renderJobBucket.forEach(renderJob -> {
            render(renderJob, commandBuffer, commandBufferIndex, renderPassBeginInfo, stack);
        });
    }

    private void render(RenderJob<?> renderJob, VkCommandBuffer commandBuffer, int commandBufferIndex, VkRenderPassBeginInfo renderPassBeginInfo, MemoryStack stack) {
        renderPassBeginInfo.sType(VK_STRUCTURE_TYPE_RENDER_PASS_BEGIN_INFO);
        renderPassBeginInfo.renderPass(renderJob.getRenderPass());
        renderPassBeginInfo.renderArea(renderJob.getRenderArea(stack));
        renderPassBeginInfo.pClearValues(renderJob.getClearValues(stack));
        renderPassBeginInfo.framebuffer(renderJob.getFramebuffer(commandBufferIndex));

        vkCmdBeginRenderPass(commandBuffer, renderPassBeginInfo, VK_SUBPASS_CONTENTS_INLINE);
        renderJob.render(commandBuffer, commandBufferIndex, stack);
        vkCmdEndRenderPass(commandBuffer);
    }

    private void cleanupCommandBuffers() {
        if (commandBuffers != null) {
            try (MemoryStack stack = stackPush()) {
                vkFreeCommandBuffers(application.getLogicalDevice(), application.getCommandPool(), BufferUtil.asPointerBuffer(commandBuffers, stack));
            }
            commandBuffers = null;
        }
    }

    public void cleanup() {
        cleanupRenderJobs();
        cleanupCommandBuffers();
        imageViews.forEach(imageView -> vkDestroyImageView(application.getLogicalDevice(), imageView, null));
        vkDestroySwapchainKHR(application.getLogicalDevice(), swapChain, null);
    }

    private void cleanupRenderJobs() {
        renderJobManager.forEachRenderJob(RenderJob::cleanup);
    }
}
