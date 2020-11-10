package com.destrostudios.icetea.core;

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
import static org.lwjgl.system.MemoryStack.stackGet;
import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.vulkan.KHRSurface.*;
import static org.lwjgl.vulkan.KHRSurface.VK_PRESENT_MODE_FIFO_KHR;
import static org.lwjgl.vulkan.KHRSwapchain.*;
import static org.lwjgl.vulkan.KHRSwapchain.vkGetSwapchainImagesKHR;
import static org.lwjgl.vulkan.VK10.*;

public class SwapChain {

    private Application application;
    @Getter
    private VkExtent2D extent;
    @Getter
    private long swapChain;
    @Getter
    private List<Long> images;
    private int imageFormat;
    private List<Long> imageViews;
    @Getter
    private long renderPass;
    private long colorImage;
    private long colorImageMemory;
    private long colorImageView;
    private long depthImage;
    private long depthImageMemory;
    private long depthImageView;
    private List<Long> framebuffers;
    @Getter
    private long descriptorPool;
    @Getter
    private List<VkCommandBuffer> commandBuffers;

    public void recreate() {
        try (MemoryStack stack = stackPush()) {
            IntBuffer width = stack.ints(0);
            IntBuffer height = stack.ints(0);
            while (width.get(0) == 0 && height.get(0) == 0) {
                glfwGetFramebufferSize(application.getWindow(), width, height);
                glfwWaitEvents();
            }
        }
        vkDeviceWaitIdle(application.getLogicalDevice());
        cleanup();
        init(application);
    }

    public void init(Application application) {
        this.application = application;
        initSwapChain();
        initImageViews();
        initRenderPass();
        initColorResources();
        initDepthResources();
        initFrameBuffers();
        initDescriptorPool();
        initGeometries();
        initCommandBuffers();
    }

    private void initSwapChain() {
        try (MemoryStack stack = stackPush()) {
            PhysicalDeviceInformation physicalDeviceInformation = application.getPhysicalDeviceInformation();
            VkSurfaceCapabilitiesKHR surfaceCapabilities = application.getPhysicalDeviceManager().getSurfaceCapabilities(stack);

            VkExtent2D extent = chooseSwapExtent(surfaceCapabilities, application.getWindow());
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
            int presentMode = chooseSwapPresentMode(physicalDeviceInformation.getSurfacePresentModes());
            swapchainCreateInfo.presentMode(presentMode);
            swapchainCreateInfo.clipped(true);
            swapchainCreateInfo.oldSwapchain(VK_NULL_HANDLE);

            LongBuffer pSwapChain = stack.longs(VK_NULL_HANDLE);
            if (vkCreateSwapchainKHR(application.getLogicalDevice(), swapchainCreateInfo, null, pSwapChain) != VK_SUCCESS) {
                throw new RuntimeException("Failed to create swap chain");
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

    private VkExtent2D chooseSwapExtent(VkSurfaceCapabilitiesKHR capabilities, long window) {
        if (capabilities.currentExtent().width() != MathUtil.UINT32_MAX) {
            return capabilities.currentExtent();
        }
        IntBuffer width = stackGet().ints(0);
        IntBuffer height = stackGet().ints(0);
        glfwGetFramebufferSize(window, width, height);
        VkExtent2D actualExtent = VkExtent2D.mallocStack().set(width.get(0), height.get(0));

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

    private int chooseSwapPresentMode(IntBuffer availablePresentModes) {
        for (int i = 0;i < availablePresentModes.capacity();i++) {
            if (availablePresentModes.get(i) == VK_PRESENT_MODE_MAILBOX_KHR) {
                return availablePresentModes.get(i);
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

    private void initRenderPass() {
        try (MemoryStack stack = stackPush()) {
            VkAttachmentDescription.Buffer attachments = VkAttachmentDescription.callocStack(3, stack);
            VkAttachmentReference.Buffer attachmentRefs = VkAttachmentReference.callocStack(3, stack);

            // Color attachments

            // MSAA Image
            VkAttachmentDescription colorAttachment = attachments.get(0);
            colorAttachment.format(imageFormat);
            colorAttachment.samples(application.getMsaaSamples());
            colorAttachment.loadOp(VK_ATTACHMENT_LOAD_OP_CLEAR);
            colorAttachment.storeOp(VK_ATTACHMENT_STORE_OP_STORE);
            colorAttachment.stencilLoadOp(VK_ATTACHMENT_LOAD_OP_DONT_CARE);
            colorAttachment.stencilStoreOp(VK_ATTACHMENT_STORE_OP_DONT_CARE);
            colorAttachment.initialLayout(VK_IMAGE_LAYOUT_UNDEFINED);
            colorAttachment.finalLayout(VK_IMAGE_LAYOUT_COLOR_ATTACHMENT_OPTIMAL);

            VkAttachmentReference colorAttachmentRef = attachmentRefs.get(0);
            colorAttachmentRef.attachment(0);
            colorAttachmentRef.layout(VK_IMAGE_LAYOUT_COLOR_ATTACHMENT_OPTIMAL);

            // Present Image
            VkAttachmentDescription colorAttachmentResolve = attachments.get(2);
            colorAttachmentResolve.format(imageFormat);
            colorAttachmentResolve.samples(VK_SAMPLE_COUNT_1_BIT);
            colorAttachmentResolve.loadOp(VK_ATTACHMENT_LOAD_OP_DONT_CARE);
            colorAttachmentResolve.storeOp(VK_ATTACHMENT_STORE_OP_STORE);
            colorAttachmentResolve.stencilLoadOp(VK_ATTACHMENT_LOAD_OP_DONT_CARE);
            colorAttachmentResolve.stencilStoreOp(VK_ATTACHMENT_STORE_OP_DONT_CARE);
            colorAttachmentResolve.initialLayout(VK_IMAGE_LAYOUT_UNDEFINED);
            colorAttachmentResolve.finalLayout(VK_IMAGE_LAYOUT_PRESENT_SRC_KHR);

            VkAttachmentReference colorAttachmentResolveRef = attachmentRefs.get(2);
            colorAttachmentResolveRef.attachment(2);
            colorAttachmentResolveRef.layout(VK_IMAGE_LAYOUT_COLOR_ATTACHMENT_OPTIMAL);

            // Depth-Stencil attachments

            VkAttachmentDescription depthAttachment = attachments.get(1);
            depthAttachment.format(findDepthFormat());
            depthAttachment.samples(application.getMsaaSamples());
            depthAttachment.loadOp(VK_ATTACHMENT_LOAD_OP_CLEAR);
            depthAttachment.storeOp(VK_ATTACHMENT_STORE_OP_DONT_CARE);
            depthAttachment.stencilLoadOp(VK_ATTACHMENT_LOAD_OP_DONT_CARE);
            depthAttachment.stencilStoreOp(VK_ATTACHMENT_STORE_OP_DONT_CARE);
            depthAttachment.initialLayout(VK_IMAGE_LAYOUT_UNDEFINED);
            depthAttachment.finalLayout(VK_IMAGE_LAYOUT_DEPTH_STENCIL_ATTACHMENT_OPTIMAL);

            VkAttachmentReference depthAttachmentRef = attachmentRefs.get(1);
            depthAttachmentRef.attachment(1);
            depthAttachmentRef.layout(VK_IMAGE_LAYOUT_DEPTH_STENCIL_ATTACHMENT_OPTIMAL);

            VkSubpassDescription.Buffer subpass = VkSubpassDescription.callocStack(1, stack);
            subpass.pipelineBindPoint(VK_PIPELINE_BIND_POINT_GRAPHICS);
            subpass.colorAttachmentCount(1);
            subpass.pColorAttachments(VkAttachmentReference.callocStack(1, stack).put(0, colorAttachmentRef));
            subpass.pDepthStencilAttachment(depthAttachmentRef);
            subpass.pResolveAttachments(VkAttachmentReference.callocStack(1, stack).put(0, colorAttachmentResolveRef));

            VkSubpassDependency.Buffer dependency = VkSubpassDependency.callocStack(1, stack);
            dependency.srcSubpass(VK_SUBPASS_EXTERNAL);
            dependency.dstSubpass(0);
            dependency.srcStageMask(VK_PIPELINE_STAGE_COLOR_ATTACHMENT_OUTPUT_BIT);
            dependency.srcAccessMask(0);
            dependency.dstStageMask(VK_PIPELINE_STAGE_COLOR_ATTACHMENT_OUTPUT_BIT);
            dependency.dstAccessMask(VK_ACCESS_COLOR_ATTACHMENT_READ_BIT | VK_ACCESS_COLOR_ATTACHMENT_WRITE_BIT);

            VkRenderPassCreateInfo renderPassCreateInfo = VkRenderPassCreateInfo.callocStack(stack);
            renderPassCreateInfo.sType(VK_STRUCTURE_TYPE_RENDER_PASS_CREATE_INFO);
            renderPassCreateInfo.pAttachments(attachments);
            renderPassCreateInfo.pSubpasses(subpass);
            renderPassCreateInfo.pDependencies(dependency);

            LongBuffer pRenderPass = stack.mallocLong(1);
            if (vkCreateRenderPass(application.getLogicalDevice(), renderPassCreateInfo, null, pRenderPass) != VK_SUCCESS) {
                throw new RuntimeException("Failed to create render pass");
            }
            renderPass = pRenderPass.get(0);
        }
    }

    private int findDepthFormat() {
        return findSupportedFormat(
            stackGet().ints(VK_FORMAT_D32_SFLOAT, VK_FORMAT_D32_SFLOAT_S8_UINT, VK_FORMAT_D24_UNORM_S8_UINT),
            VK_IMAGE_TILING_OPTIMAL,
            VK_FORMAT_FEATURE_DEPTH_STENCIL_ATTACHMENT_BIT
        );
    }

    private int findSupportedFormat(IntBuffer formatCandidates, int tiling, int features) {
        try (MemoryStack stack = stackPush()) {
            VkFormatProperties props = VkFormatProperties.callocStack(stack);
            for (int i = 0; i < formatCandidates.capacity(); ++i) {
                int format = formatCandidates.get(i);
                vkGetPhysicalDeviceFormatProperties(application.getPhysicalDevice(), format, props);
                if ((tiling == VK_IMAGE_TILING_LINEAR) && ((props.linearTilingFeatures() & features) == features)) {
                    return format;
                } else if ((tiling == VK_IMAGE_TILING_OPTIMAL) && ((props.optimalTilingFeatures() & features) == features)) {
                    return format;
                }
            }
        }
        throw new RuntimeException("Failed to find supported format");
    }

    private void initColorResources() {
        try (MemoryStack stack = stackPush()) {
            LongBuffer pColorImage = stack.mallocLong(1);
            LongBuffer pColorImageMemory = stack.mallocLong(1);
            application.getImageManager().createImage(
                extent.width(),
                extent.height(),
                1,
                application.getMsaaSamples(),
                imageFormat,
                VK_IMAGE_TILING_OPTIMAL,
                VK_IMAGE_USAGE_TRANSIENT_ATTACHMENT_BIT | VK_IMAGE_USAGE_COLOR_ATTACHMENT_BIT,
                VK_MEMORY_PROPERTY_DEVICE_LOCAL_BIT,
                pColorImage,
                pColorImageMemory
            );

            colorImage = pColorImage.get(0);
            colorImageMemory = pColorImageMemory.get(0);
            colorImageView = application.getImageManager().createImageView(colorImage, imageFormat, VK_IMAGE_ASPECT_COLOR_BIT, 1);

            application.getImageManager().transitionImageLayout(colorImage, imageFormat, VK_IMAGE_LAYOUT_UNDEFINED, VK_IMAGE_LAYOUT_COLOR_ATTACHMENT_OPTIMAL, 1);
        }
    }

    private void initDepthResources() {
        try (MemoryStack stack = stackPush()) {
            int depthFormat = findDepthFormat();
            LongBuffer pDepthImage = stack.mallocLong(1);
            LongBuffer pDepthImageMemory = stack.mallocLong(1);
            application.getImageManager().createImage(
                extent.width(),
                extent.height(),
                1,
                application.getMsaaSamples(),
                depthFormat,
                VK_IMAGE_TILING_OPTIMAL,
                VK_IMAGE_USAGE_DEPTH_STENCIL_ATTACHMENT_BIT,
                VK_MEMORY_PROPERTY_DEVICE_LOCAL_BIT,
                pDepthImage,
                pDepthImageMemory
            );

            depthImage = pDepthImage.get(0);
            depthImageMemory = pDepthImageMemory.get(0);
            depthImageView = application.getImageManager().createImageView(depthImage, depthFormat, VK_IMAGE_ASPECT_DEPTH_BIT, 1);

            application.getImageManager().transitionImageLayout(depthImage, depthFormat, VK_IMAGE_LAYOUT_UNDEFINED, VK_IMAGE_LAYOUT_DEPTH_STENCIL_ATTACHMENT_OPTIMAL, 1);
        }
    }

    private void initFrameBuffers() {
        framebuffers = new ArrayList<>(images.size());
        try (MemoryStack stack = stackPush()) {
            LongBuffer attachments = stack.longs(colorImageView, depthImageView, VK_NULL_HANDLE);
            LongBuffer pFrameBuffer = stack.mallocLong(1);

            VkFramebufferCreateInfo framebufferCreateInfo = VkFramebufferCreateInfo.callocStack(stack);
            framebufferCreateInfo.sType(VK_STRUCTURE_TYPE_FRAMEBUFFER_CREATE_INFO);
            framebufferCreateInfo.renderPass(renderPass);
            framebufferCreateInfo.width(extent.width());
            framebufferCreateInfo.height(extent.height());
            framebufferCreateInfo.layers(1);

            for (long imageView : imageViews) {
                attachments.put(2, imageView);
                framebufferCreateInfo.pAttachments(attachments);
                if (vkCreateFramebuffer(application.getLogicalDevice(), framebufferCreateInfo, null, pFrameBuffer) != VK_SUCCESS) {
                    throw new RuntimeException("Failed to create framebuffer");
                }
                framebuffers.add(pFrameBuffer.get(0));
            }
        }
    }

    private void initDescriptorPool() {
        try (MemoryStack stack = stackPush()) {
            VkDescriptorPoolSize.Buffer poolSizes = VkDescriptorPoolSize.callocStack(2, stack);

            // TODO: Has to be dynamic at some point, currently the scene is fixed
            int descriptorSetsCount = application.getGeometries().size() * images.size();

            VkDescriptorPoolSize uniformBufferPoolSize = poolSizes.get(0);
            uniformBufferPoolSize.type(VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER);
            uniformBufferPoolSize.descriptorCount(descriptorSetsCount);

            VkDescriptorPoolSize textureSamplerPoolSize = poolSizes.get(1);
            textureSamplerPoolSize.type(VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER);
            textureSamplerPoolSize.descriptorCount(descriptorSetsCount);

            VkDescriptorPoolCreateInfo poolCreateInfo = VkDescriptorPoolCreateInfo.callocStack(stack);
            poolCreateInfo.sType(VK_STRUCTURE_TYPE_DESCRIPTOR_POOL_CREATE_INFO);
            poolCreateInfo.pPoolSizes(poolSizes);
            poolCreateInfo.maxSets(descriptorSetsCount);

            LongBuffer pDescriptorPool = stack.mallocLong(1);
            if (vkCreateDescriptorPool(application.getLogicalDevice(), poolCreateInfo, null, pDescriptorPool) != VK_SUCCESS) {
                throw new RuntimeException("Failed to create descriptor pool");
            }
            descriptorPool = pDescriptorPool.get(0);
        }
    }

    private void initGeometries() {
        for (Geometry geometry : application.getGeometries()) {
            geometry.initGraphicsPipeline();
            geometry.onSwapChainCreation();
        }
    }

    private void initCommandBuffers() {
        int commandBuffersCount = framebuffers.size();
        commandBuffers = new ArrayList<>(commandBuffersCount);
        try (MemoryStack stack = stackPush()) {
            VkCommandBufferAllocateInfo allocInfo = VkCommandBufferAllocateInfo.callocStack(stack);
            allocInfo.sType(VK_STRUCTURE_TYPE_COMMAND_BUFFER_ALLOCATE_INFO);
            allocInfo.commandPool(application.getCommandPool());
            allocInfo.level(VK_COMMAND_BUFFER_LEVEL_PRIMARY);
            allocInfo.commandBufferCount(commandBuffersCount);

            PointerBuffer pCommandBuffers = stack.mallocPointer(commandBuffersCount);
            if (vkAllocateCommandBuffers(application.getLogicalDevice(), allocInfo, pCommandBuffers) != VK_SUCCESS) {
                throw new RuntimeException("Failed to allocate command buffers");
            }

            for (int i = 0; i < commandBuffersCount; i++) {
                commandBuffers.add(new VkCommandBuffer(pCommandBuffers.get(i), application.getLogicalDevice()));
            }

            VkCommandBufferBeginInfo bufferBeginInfo = VkCommandBufferBeginInfo.callocStack(stack);
            bufferBeginInfo.sType(VK_STRUCTURE_TYPE_COMMAND_BUFFER_BEGIN_INFO);

            VkRenderPassBeginInfo renderPassBeginInfo = VkRenderPassBeginInfo.callocStack(stack);
            renderPassBeginInfo.sType(VK_STRUCTURE_TYPE_RENDER_PASS_BEGIN_INFO);
            renderPassBeginInfo.renderPass(renderPass);

            VkRect2D renderArea = VkRect2D.callocStack(stack);
            renderArea.offset(VkOffset2D.callocStack(stack).set(0, 0));
            renderArea.extent(extent);
            renderPassBeginInfo.renderArea(renderArea);

            VkClearValue.Buffer clearValues = VkClearValue.callocStack(2, stack);
            clearValues.get(0).color().float32(stack.floats(0.0f, 0.0f, 0.0f, 1.0f));
            clearValues.get(1).depthStencil().set(1.0f, 0);
            renderPassBeginInfo.pClearValues(clearValues);

            for(int i = 0;i < commandBuffersCount;i++) {
                VkCommandBuffer commandBuffer = commandBuffers.get(i);
                if (vkBeginCommandBuffer(commandBuffer, bufferBeginInfo) != VK_SUCCESS) {
                    throw new RuntimeException("Failed to begin recording command buffer");
                }
                renderPassBeginInfo.framebuffer(framebuffers.get(i));
                vkCmdBeginRenderPass(commandBuffer, renderPassBeginInfo, VK_SUBPASS_CONTENTS_INLINE);

                for (Geometry geometry : application.getGeometries()) {
                    GraphicsPipeline graphicsPipeline = geometry.getGraphicsPipeline();
                    vkCmdBindPipeline(commandBuffer, VK_PIPELINE_BIND_POINT_GRAPHICS, graphicsPipeline.getGraphicsPipeline());
                    LongBuffer vertexBuffers = stack.longs(geometry.getVertexBuffer());
                    LongBuffer offsets = stack.longs(0);
                    vkCmdBindVertexBuffers(commandBuffer, 0, vertexBuffers, offsets);
                    vkCmdBindIndexBuffer(commandBuffer, geometry.getIndexBuffer(), 0, VK_INDEX_TYPE_UINT32);
                    vkCmdBindDescriptorSets(commandBuffer, VK_PIPELINE_BIND_POINT_GRAPHICS, graphicsPipeline.getPipelineLayout(), 0, stack.longs(geometry.getDescriptorSets().get(i)), null);
                    vkCmdDrawIndexed(commandBuffer, geometry.getIndices().length, 1, 0, 0, 0);
                }

                vkCmdEndRenderPass(commandBuffer);
                if (vkEndCommandBuffer(commandBuffer) != VK_SUCCESS) {
                    throw new RuntimeException("Failed to record command buffer");
                }
            }
        }
    }

    public void cleanup() {
        vkDestroyImageView(application.getLogicalDevice(), colorImageView, null);
        vkDestroyImage(application.getLogicalDevice(), colorImage, null);
        vkFreeMemory(application.getLogicalDevice(), colorImageMemory, null);

        vkDestroyImageView(application.getLogicalDevice(), depthImageView, null);
        vkDestroyImage(application.getLogicalDevice(), depthImage, null);
        vkFreeMemory(application.getLogicalDevice(), depthImageMemory, null);

        for (Geometry geometry : application.getGeometries()) {
            geometry.cleanupSwapChainDependencies();
        }

        vkDestroyDescriptorPool(application.getLogicalDevice(), descriptorPool, null);

        framebuffers.forEach(framebuffer -> vkDestroyFramebuffer(application.getLogicalDevice(), framebuffer, null));

        vkFreeCommandBuffers(application.getLogicalDevice(), application.getCommandPool(), BufferUtil.asPointerBuffer(commandBuffers));

        for (Geometry geometry : application.getGeometries()) {
            GraphicsPipeline graphicsPipeline = geometry.getGraphicsPipeline();
            vkDestroyPipeline(application.getLogicalDevice(), graphicsPipeline.getGraphicsPipeline(), null);
            vkDestroyPipelineLayout(application.getLogicalDevice(), graphicsPipeline.getPipelineLayout(), null);
        }

        vkDestroyRenderPass(application.getLogicalDevice(), renderPass, null);

        imageViews.forEach(imageView -> vkDestroyImageView(application.getLogicalDevice(), imageView, null));

        vkDestroySwapchainKHR(application.getLogicalDevice(), swapChain, null);
    }
}
