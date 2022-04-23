package com.destrostudios.icetea.core;

import com.destrostudios.icetea.core.lifecycle.LifecycleObject;
import com.destrostudios.icetea.core.render.GeometryRenderContext;
import com.destrostudios.icetea.core.render.RenderAction;
import com.destrostudios.icetea.core.render.RenderJob;
import com.destrostudios.icetea.core.render.RenderJobManager;
import com.destrostudios.icetea.core.util.BufferUtil;
import com.destrostudios.icetea.core.util.MathUtil;
import lombok.Getter;
import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.IntBuffer;
import java.nio.LongBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.lwjgl.glfw.GLFW.glfwGetFramebufferSize;
import static org.lwjgl.glfw.GLFW.glfwWaitEvents;
import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.vulkan.KHRSurface.*;
import static org.lwjgl.vulkan.KHRSurface.VK_PRESENT_MODE_FIFO_KHR;
import static org.lwjgl.vulkan.KHRSwapchain.*;
import static org.lwjgl.vulkan.KHRSwapchain.vkGetSwapchainImagesKHR;
import static org.lwjgl.vulkan.VK10.*;

public class SwapChain extends LifecycleObject {

    private static final Logger LOGGER = LoggerFactory.getLogger(SwapChain.class);

    public SwapChain() {
        renderJobManager = new RenderJobManager();
    }
    private static final int FRAMES_IN_FLIGHT = 2;
    @Getter
    private VkExtent2D extent;
    private long swapChain;
    @Getter
    private ArrayList<Long> images;
    @Getter
    private int imageFormat;
    @Getter
    private ArrayList<Long> imageViews;
    private ArrayList<VkCommandBuffer> commandBuffers;
    @Getter
    private RenderJobManager renderJobManager;
    private Frame[] inFlightFrames;
    private Map<Integer, Frame> imagesInFlight;
    private int currentFrame;
    private boolean wasResized;
    private boolean isDuringRecreation;

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
        Application tmpApplication = application;
        // TODO: This is a workaround to prevent inFlightFrame recreation (not allowed while actively rendering) during swapchain recreation
        isDuringRecreation = true;
        cleanup();
        isDuringRecreation = false;
        update(tmpApplication, 0, 0);
        recordCommandBuffers();
    }

    @Override
    protected void init() {
        LOGGER.debug("Initializing swapchain...");
        super.init();
        initSwapChain();
        initImageViews();
        initRenderJobs();
        initCommandBuffers();
        initSyncObjects();
        LOGGER.debug("Initialized swapchain.");
    }

    private void initSwapChain() {
        try (MemoryStack stack = stackPush()) {
            PhysicalDeviceInformation physicalDeviceInformation = application.getPhysicalDeviceInformation();

            // TODO: Can this only be done once initially like the rest of PhysicalDeviceInformation?
            LOGGER.debug("Fetching physical device surface capabilities...");
            VkSurfaceCapabilitiesKHR surfaceCapabilities = VkSurfaceCapabilitiesKHR.mallocStack(stack);
            vkGetPhysicalDeviceSurfaceCapabilitiesKHR(application.getPhysicalDevice(), application.getSurface(), surfaceCapabilities);
            LOGGER.debug("Fetched physical device surface capabilities.");

            VkExtent2D extent = chooseSwapExtent(surfaceCapabilities, application.getWindow(), stack);
            this.extent = VkExtent2D.create().set(extent);

            VkSwapchainCreateInfoKHR swapchainCreateInfo = VkSwapchainCreateInfoKHR.callocStack(stack);
            swapchainCreateInfo.sType(VK_STRUCTURE_TYPE_SWAPCHAIN_CREATE_INFO_KHR);
            swapchainCreateInfo.surface(application.getSurface());

            int surfaceMinImagesCount = surfaceCapabilities.minImageCount();
            LOGGER.debug("Surface minimum image count: {}", surfaceMinImagesCount);
            int surfaceMaxImagesCount = surfaceCapabilities.maxImageCount();
            LOGGER.debug("Surface maximum image count: {}", surfaceMaxImagesCount);
            IntBuffer imageCount = stack.ints(surfaceMinImagesCount + 1);
            if ((surfaceMaxImagesCount > 0) && (imageCount.get(0) > surfaceMaxImagesCount)) {
                imageCount.put(0, surfaceMaxImagesCount);
            }
            LOGGER.debug("Choosing image count: {}", imageCount.get(0));
            swapchainCreateInfo.minImageCount(imageCount.get(0));

            VkSurfaceFormatKHR surfaceFormat = chooseSurfaceFormat(physicalDeviceInformation.getSurfaceFormats());
            imageFormat = surfaceFormat.format();
            int colorSpace = surfaceFormat.colorSpace();
            LOGGER.debug("Surface format: Format = {}, ColorSpace = {}", imageFormat, colorSpace);
            swapchainCreateInfo.imageFormat(imageFormat);
            swapchainCreateInfo.imageColorSpace(colorSpace);
            swapchainCreateInfo.imageExtent(extent);
            swapchainCreateInfo.imageArrayLayers(1);
            swapchainCreateInfo.imageUsage(VK_IMAGE_USAGE_COLOR_ATTACHMENT_BIT);

            if (physicalDeviceInformation.getQueueFamilyIndexGraphics() != physicalDeviceInformation.getQueueFamilyIndexSurface()) {
                LOGGER.debug("Image sharing mode: Concurrent");
                swapchainCreateInfo.imageSharingMode(VK_SHARING_MODE_CONCURRENT);
                swapchainCreateInfo.pQueueFamilyIndices(stack.ints(physicalDeviceInformation.getQueueFamilyIndexGraphics(), physicalDeviceInformation.getQueueFamilyIndexSurface()));
            } else {
                LOGGER.debug("Image sharing mode: Exclusive");
                swapchainCreateInfo.imageSharingMode(VK_SHARING_MODE_EXCLUSIVE);
            }

            swapchainCreateInfo.preTransform(surfaceCapabilities.currentTransform());
            swapchainCreateInfo.compositeAlpha(VK_COMPOSITE_ALPHA_OPAQUE_BIT_KHR);
            int presentMode = choosePresentMode(physicalDeviceInformation.getSurfacePresentModes(), application.getConfig().getPreferredPresentMode());
            swapchainCreateInfo.presentMode(presentMode);
            swapchainCreateInfo.clipped(true);
            swapchainCreateInfo.oldSwapchain(VK_NULL_HANDLE);

            LongBuffer pSwapChain = stack.longs(VK_NULL_HANDLE);
            LOGGER.debug("Creating swapchain...");
            int result = vkCreateSwapchainKHR(application.getLogicalDevice(), swapchainCreateInfo, null, pSwapChain);
            LOGGER.debug("Created swapchain.");
            if (result != VK_SUCCESS) {
                throw new RuntimeException("Failed to create swapchain (result = " + result + ")");
            }
            swapChain = pSwapChain.get(0);

            LOGGER.debug("Fetching swapchain images...");
            vkGetSwapchainImagesKHR(application.getLogicalDevice(), swapChain, imageCount, null);
            LOGGER.debug("Found {} swapchain images.", imageCount.get(0));
            LongBuffer pImages = stack.mallocLong(imageCount.get(0));
            vkGetSwapchainImagesKHR(application.getLogicalDevice(), swapChain, imageCount, pImages);
            LOGGER.debug("Fetched {} swapchain images.", imageCount.get(0));
            images = new ArrayList<>(imageCount.get(0));
            for (int i = 0;i < pImages.capacity();i++) {
                images.add(pImages.get(i));
            }
        }
    }

    private VkExtent2D chooseSwapExtent(VkSurfaceCapabilitiesKHR capabilities, long window, MemoryStack stack) {
        VkExtent2D currentExtent = capabilities.currentExtent();
        if (currentExtent.width() != MathUtil.UINT32_MAX) {
            LOGGER.debug("Using available current extent: {} x {}", currentExtent.width(), currentExtent.height());
            return currentExtent;
        }
        IntBuffer width = stack.ints(0);
        IntBuffer height = stack.ints(0);
        LOGGER.debug("Fetching framebuffer size...");
        glfwGetFramebufferSize(window, width, height);
        LOGGER.debug("Fetched framebuffer size: {} x {}", width.get(0), height.get(0));
        VkExtent2D actualExtent = VkExtent2D.mallocStack(stack).set(width.get(0), height.get(0));

        VkExtent2D minExtent = capabilities.minImageExtent();
        VkExtent2D maxExtent = capabilities.maxImageExtent();

        actualExtent.width(MathUtil.clamp(minExtent.width(), maxExtent.width(), actualExtent.width()));
        actualExtent.height(MathUtil.clamp(minExtent.height(), maxExtent.height(), actualExtent.height()));
        LOGGER.debug("Calculated actual extent: {} x {}", actualExtent.width(), actualExtent.height());

        return actualExtent;
    }

    private VkSurfaceFormatKHR chooseSurfaceFormat(VkSurfaceFormatKHR.Buffer availableFormats) {
        // TODO: Should have a proper ranking logic (preferring nonlinear) and not just have one preferred combination and default to the first one otherwise
        // TODO: Also, if we happen to use a linear color space, we have to adjust the read texture pixels and stored image formats as a transformation back to sRGB at the end doesn't happen automatically yet
        return availableFormats.stream()
                .filter(availableFormat -> availableFormat.format() == VK_FORMAT_B8G8R8A8_SRGB)
                .filter(availableFormat -> availableFormat.colorSpace() == VK_COLOR_SPACE_SRGB_NONLINEAR_KHR)
                .findAny()
                .orElse(availableFormats.get(0));
    }

    private int choosePresentMode(IntBuffer availablePresentModes, int preferredPresentMode) {
        LOGGER.debug("Choosing present mode...");
        for (int i = 0; i < availablePresentModes.capacity(); i++) {
            int presentMode = availablePresentModes.get(i);
            if (presentMode == preferredPresentMode) {
                LOGGER.debug("Choosing preferred present mode: {}", presentMode);
                return presentMode;
            }
        }
        LOGGER.debug("Preferred present mode not available, defaulting to FIFO.");
        return VK_PRESENT_MODE_FIFO_KHR;
    }

    private void initImageViews() {
        LOGGER.debug("Initializing image views...");
        imageViews = new ArrayList<>(images.size());
        for (long swapChainImage : images) {
            imageViews.add(application.getImageManager().createImageView(swapChainImage, imageFormat, VK_IMAGE_ASPECT_COLOR_BIT, 1));
        }
        LOGGER.debug("Initialized image views.");
    }

    public void recreateRenderJobs() {
        cleanupRenderJobs();
        initRenderJobs();
    }

    public void initRenderJobs() {
        LOGGER.debug("Initializing render jobs...");
        // Make sure all render jobs are initialized first, because they can have dependencies between each other
        renderJobManager.forEachRenderJob(renderJob -> renderJob.update(application, 0, 0));
        renderJobManager.forEachRenderJob(renderJob -> {
            application.getRootNode().forEachGeometry(geometry -> {
                GeometryRenderContext<?> renderContext = geometry.getRenderContext(renderJob);
                if (renderContext != null) {
                    renderContext.createDescriptorDependencies();
                }
            });
        });
        LOGGER.debug("Initialized render jobs.");
    }

    private void initCommandBuffers() {
        try (MemoryStack stack = stackPush()) {
            LOGGER.debug("Initializing command buffers...");
            int commandBuffersCount = images.size();
            commandBuffers = new ArrayList<>(commandBuffersCount);
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
            LOGGER.debug("Initialized command buffers.");
        }
    }

    private void initSyncObjects() {
        LOGGER.debug("Initializing sync objects...");
        inFlightFrames = new Frame[FRAMES_IN_FLIGHT];
        imagesInFlight = new HashMap<>(images.size());
        try (MemoryStack stack = stackPush()) {
            VkSemaphoreCreateInfo semaphoreInfo = VkSemaphoreCreateInfo.callocStack(stack);
            semaphoreInfo.sType(VK_STRUCTURE_TYPE_SEMAPHORE_CREATE_INFO);

            VkFenceCreateInfo fenceInfo = VkFenceCreateInfo.callocStack(stack);
            fenceInfo.sType(VK_STRUCTURE_TYPE_FENCE_CREATE_INFO);
            fenceInfo.flags(VK_FENCE_CREATE_SIGNALED_BIT);

            LongBuffer pImageAvailableSemaphore = stack.mallocLong(1);
            LongBuffer pRenderFinishedSemaphore = stack.mallocLong(1);
            LongBuffer pFence = stack.mallocLong(1);

            for (int i = 0; i < inFlightFrames.length; i++) {
                int result = vkCreateSemaphore(application.getLogicalDevice(), semaphoreInfo, null, pImageAvailableSemaphore);
                if (result != VK_SUCCESS) {
                    throw new RuntimeException("Failed to create image available semaphore for the frame " + i + " (result = " + result + ")");
                }
                result = vkCreateSemaphore(application.getLogicalDevice(), semaphoreInfo, null, pRenderFinishedSemaphore);
                if (result != VK_SUCCESS) {
                    throw new RuntimeException("Failed to create render finished semaphore for the frame " + i + " (result = " + result + ")");
                }
                result = vkCreateFence(application.getLogicalDevice(), fenceInfo, null, pFence);
                if (result != VK_SUCCESS) {
                    throw new RuntimeException("Failed to create fence for the frame " + i + " (result = " + result + ")");
                }
                inFlightFrames[i] = new Frame(pImageAvailableSemaphore.get(0), pRenderFinishedSemaphore.get(0), pFence.get(0));
            }
        }
        LOGGER.debug("Initialized sync objects.");
    }

    public void recordCommandBuffers() {
        try (MemoryStack stack = stackPush()) {
            VkCommandBufferBeginInfo bufferBeginInfo = VkCommandBufferBeginInfo.callocStack(stack);
            bufferBeginInfo.sType(VK_STRUCTURE_TYPE_COMMAND_BUFFER_BEGIN_INFO);

            VkRenderPassBeginInfo renderPassBeginInfo = VkRenderPassBeginInfo.callocStack(stack);
            renderPassBeginInfo.sType(VK_STRUCTURE_TYPE_RENDER_PASS_BEGIN_INFO);
            for (VkCommandBuffer commandBuffer : commandBuffers) {
                int result = vkBeginCommandBuffer(commandBuffer, bufferBeginInfo);
                if (result != VK_SUCCESS) {
                    throw new RuntimeException("Failed to begin recording command buffer (result = " + result + ")");
                }
            }
            render(renderJobManager.getQueuePreScene(), renderPassBeginInfo);
            render(renderJobManager.getSceneRenderJob(), renderPassBeginInfo);
            render(renderJobManager.getQueuePostScene(), renderPassBeginInfo);
            for (VkCommandBuffer commandBuffer : commandBuffers) {
                int result = vkEndCommandBuffer(commandBuffer);
                if (result != VK_SUCCESS) {
                    throw new RuntimeException("Failed to record command buffer (result = " + result + ")");
                }
            }
        }
    }

    private void render(List<RenderJob<?>> renderJobBucket, VkRenderPassBeginInfo renderPassBeginInfo) {
        renderJobBucket.forEach(renderJob -> render(renderJob, renderPassBeginInfo));
    }

    private void render(RenderJob<?> renderJob, VkRenderPassBeginInfo renderPassBeginInfo) {
        try (MemoryStack stack = stackPush()) {
            renderPassBeginInfo.renderPass(renderJob.getRenderPass());
            renderPassBeginInfo.renderArea(renderJob.getRenderArea(stack));
            renderPassBeginInfo.pClearValues(renderJob.getClearValues(stack));
            for (int i = 0; i < commandBuffers.size(); i++) {
                renderPassBeginInfo.framebuffer(renderJob.getFramebuffer(i));
                vkCmdBeginRenderPass(commandBuffers.get(i), renderPassBeginInfo, VK_SUBPASS_CONTENTS_INLINE);
            }
        }
        renderJob.render(this::render);
        for (VkCommandBuffer commandBuffer : commandBuffers) {
            vkCmdEndRenderPass(commandBuffer);
        }
    }

    private void render(RenderAction renderAction) {
        for (int i = 0; i < commandBuffers.size(); i++) {
            renderAction.render(commandBuffers.get(i), i);
        }
    }

    @Override
    public void update(int imageIndex, float tpf) {
        super.update(imageIndex, tpf);
        renderJobManager.forEachRenderJob(renderJob -> renderJob.update(application, imageIndex, tpf));
    }

    public int acquireNextImageIndex() {
        try (MemoryStack stack = stackPush()) {
            Frame thisFrame = inFlightFrames[currentFrame];
            IntBuffer pImageIndex = stack.mallocInt(1);
            int result = vkAcquireNextImageKHR(
                application.getLogicalDevice(),
                swapChain,
                MathUtil.UINT64_MAX,
                thisFrame.getImageAvailableSemaphore(),
                VK_NULL_HANDLE,
                pImageIndex
            );
            if (result == VK_ERROR_OUT_OF_DATE_KHR) {
                recreate();
                return -1;
            } else if (result != VK_SUCCESS) {
                throw new RuntimeException("Cannot get image (result = " + result + ")");
            }
            return pImageIndex.get(0);
        }
    }

    public void onResize() {
        wasResized = true;
    }

    public void drawFrame(int imageIndex) {
        try (MemoryStack stack = stackPush()) {
            Frame thisFrame = inFlightFrames[currentFrame];

            if (imagesInFlight.containsKey(imageIndex)) {
                vkWaitForFences(application.getLogicalDevice(), imagesInFlight.get(imageIndex).getFence(), true, MathUtil.UINT64_MAX);
            }
            imagesInFlight.put(imageIndex, thisFrame);

            VkSubmitInfo submitInfo = VkSubmitInfo.callocStack(stack);
            submitInfo.sType(VK_STRUCTURE_TYPE_SUBMIT_INFO);
            submitInfo.waitSemaphoreCount(1);
            submitInfo.pWaitSemaphores(stack.longs(thisFrame.getImageAvailableSemaphore()));
            submitInfo.pWaitDstStageMask(stack.ints(VK_PIPELINE_STAGE_COLOR_ATTACHMENT_OUTPUT_BIT));
            LongBuffer pRenderFinishedSemaphore = stack.longs(thisFrame.getRenderFinishedSemaphore());
            submitInfo.pSignalSemaphores(pRenderFinishedSemaphore);
            submitInfo.pCommandBuffers(stack.pointers(commandBuffers.get(imageIndex)));
            LongBuffer pFence = stack.longs(thisFrame.getFence());
            vkResetFences(application.getLogicalDevice(), pFence);
            int result = vkQueueSubmit(application.getGraphicsQueue(), submitInfo, thisFrame.getFence());
            if (result != VK_SUCCESS) {
                throw new RuntimeException("Failed to submit draw command buffer (result = " + result + ")");
            }

            VkPresentInfoKHR presentInfo = VkPresentInfoKHR.callocStack(stack);
            presentInfo.sType(VK_STRUCTURE_TYPE_PRESENT_INFO_KHR);
            presentInfo.pWaitSemaphores(pRenderFinishedSemaphore);
            presentInfo.swapchainCount(1);
            presentInfo.pSwapchains(stack.longs(swapChain));
            presentInfo.pImageIndices(stack.ints(imageIndex));
            result = vkQueuePresentKHR(application.getPresentQueue(), presentInfo);
            if ((result == VK_ERROR_OUT_OF_DATE_KHR) || (result == VK_SUBOPTIMAL_KHR) || wasResized) {
                wasResized = false;
                recreate();
            } else if (result != VK_SUCCESS) {
                throw new RuntimeException("Failed to present swapchain image (result = " + result + ")");
            }

            currentFrame = ((currentFrame + 1) % inFlightFrames.length);

            // Wait for GPU to be finished, so we can safely access memory in our logic again (e.g. freeing memory of removed objects)
            vkWaitForFences(application.getLogicalDevice(), pFence, true, MathUtil.UINT64_MAX);
        }
    }

    @Override
    protected void cleanupInternal() {
        if (!isDuringRecreation) {
            cleanupInFlightFrames();
        }
        cleanupRenderJobs();
        cleanupCommandBuffers();
        imageViews.forEach(imageView -> vkDestroyImageView(application.getLogicalDevice(), imageView, null));
        vkDestroySwapchainKHR(application.getLogicalDevice(), swapChain, null);
        super.cleanupInternal();
    }

    private void cleanupInFlightFrames() {
        for (Frame frame : inFlightFrames) {
            vkDestroySemaphore(application.getLogicalDevice(), frame.getRenderFinishedSemaphore(), null);
            vkDestroySemaphore(application.getLogicalDevice(), frame.getImageAvailableSemaphore(), null);
            vkDestroyFence(application.getLogicalDevice(), frame.getFence(), null);
        }
    }

    private void cleanupRenderJobs() {
        renderJobManager.forEachRenderJob(RenderJob::cleanup);
    }

    private void cleanupCommandBuffers() {
        if (commandBuffers != null) {
            try (MemoryStack stack = stackPush()) {
                vkFreeCommandBuffers(application.getLogicalDevice(), application.getCommandPool(), BufferUtil.asPointerBuffer(commandBuffers, stack));
            }
            commandBuffers = null;
        }
    }
}
