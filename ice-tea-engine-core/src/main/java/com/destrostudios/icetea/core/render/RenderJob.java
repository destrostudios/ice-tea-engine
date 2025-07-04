package com.destrostudios.icetea.core.render;

import com.destrostudios.icetea.core.object.NativeObject;
import com.destrostudios.icetea.core.texture.Texture;
import lombok.Getter;
import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.*;

import java.nio.IntBuffer;
import java.nio.LongBuffer;
import java.util.*;
import java.util.function.Function;

import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.util.vma.Vma.*;
import static org.lwjgl.vulkan.VK10.*;

public abstract class RenderJob<RPC extends RenderPipelineCreator<?, ?>> extends NativeObject {

    public RenderJob(String name) {
        this.name = name;
    }
    @Getter
    private String name;
    @Getter
    protected VkExtent2D extent;
    @Getter
    protected RPC renderPipelineCreator;
    @Getter
    protected long renderPass;
    protected List<Long> frameBuffers;

    @Override
    protected void initNative() {
        super.initNative();
        extent = calculateExtent();
    }

    protected abstract VkExtent2D calculateExtent();

    protected long getPotentiallyPresentingColorImageView(Texture colorTexture, int frameBufferIndex) {
        if (isPresentingRenderJob()) {
            return application.getSwapChain().getImageViews().get(frameBufferIndex);
        } else {
            return colorTexture.getImageView();
        }
    }

    protected void initFrameBuffers(Function<Integer, long[]> getAttachmentsByFrameBufferIndex) {
        int frameBuffersCount = (isPresentingRenderJob() ? application.getSwapChain().getImages().size() : 1);
        initFrameBuffers(getAttachmentsByFrameBufferIndex, frameBuffersCount);
    }

    protected void initFrameBuffers(Function<Integer, long[]> getAttachmentsByFrameBufferIndex, int frameBuffersCount) {
        try (MemoryStack stack = stackPush()) {
            VkFramebufferCreateInfo framebufferCreateInfo = VkFramebufferCreateInfo.callocStack(stack);
            framebufferCreateInfo.sType(VK_STRUCTURE_TYPE_FRAMEBUFFER_CREATE_INFO);
            framebufferCreateInfo.renderPass(renderPass);
            framebufferCreateInfo.width(extent.width());
            framebufferCreateInfo.height(extent.height());
            framebufferCreateInfo.layers(1);

            frameBuffers = new ArrayList<>(frameBuffersCount);
            for (int i = 0; i < frameBuffersCount; i++) {
                framebufferCreateInfo.pAttachments(stack.longs(getAttachmentsByFrameBufferIndex.apply(i)));

                LongBuffer pFrameBuffer = stack.mallocLong(1);
                int result = vkCreateFramebuffer(application.getLogicalDevice(), framebufferCreateInfo, null, pFrameBuffer);
                if (result != VK_SUCCESS) {
                    throw new RuntimeException("Failed to create framebuffer (result = " + result + ")");
                }
                frameBuffers.add(pFrameBuffer.get(0));
            }
        }
    }

    public List<Long> getFrameBuffersToRender(int imageIndex) {
        LinkedList<Long> frameBuffersToRender = new LinkedList<>();
        frameBuffersToRender.add(frameBuffers.get(isPresentingRenderJob() ? imageIndex : 0));
        return frameBuffersToRender;
    }

    protected boolean isPresentingRenderJob() {
        return (this == application.getSwapChain().getRenderJobManager().getPresentingRenderJob());
    }

    public VkRect2D getRenderArea(MemoryStack stack) {
        VkRect2D renderArea = VkRect2D.callocStack(stack);
        renderArea.offset(VkOffset2D.callocStack(stack).set(0, 0));
        renderArea.extent(extent);
        return renderArea;
    }

    public VkClearValue.Buffer getClearValues(MemoryStack stack) {
        return null;
    }

    public abstract List<RenderTask> render();

    public void afterAllRenderJobsUpdatedNative() {

    }

    @Override
    protected void cleanupNativeInternal() {
        frameBuffers.forEach(frameBuffer -> vkDestroyFramebuffer(application.getLogicalDevice(), frameBuffer, null));
        vkDestroyRenderPass(application.getLogicalDevice(), renderPass, null);
        super.cleanupNativeInternal();
    }

    // Helper methods

    protected void initColorTexture(Texture texture) {
        try (MemoryStack stack = stackPush()) {
            int imageFormat = application.getSwapChain().getImageFormat();

            LongBuffer pColorImage = stack.mallocLong(1);
            PointerBuffer pColorImageAllocation = stack.mallocPointer(1);
            application.getImageManager().createImage(
                extent.width(),
                extent.height(),
                1,
                VK_SAMPLE_COUNT_1_BIT,
                imageFormat,
                VK_IMAGE_USAGE_COLOR_ATTACHMENT_BIT | VK_IMAGE_USAGE_SAMPLED_BIT,
                VMA_MEMORY_USAGE_AUTO_PREFER_DEVICE,
                1,
                pColorImage,
                pColorImageAllocation
            );
            long image = pColorImage.get(0);
            long imageAllocation = pColorImageAllocation.get(0);

            long imageView = application.getImageManager().createImageView(image, imageFormat, VK_IMAGE_ASPECT_COLOR_BIT, 1);

            VkSamplerCreateInfo samplerCreateInfo = VkSamplerCreateInfo.callocStack(stack);
            samplerCreateInfo.sType(VK_STRUCTURE_TYPE_SAMPLER_CREATE_INFO);
            samplerCreateInfo.magFilter(VK_FILTER_LINEAR);
            samplerCreateInfo.minFilter(VK_FILTER_LINEAR);
            samplerCreateInfo.addressModeU(VK_SAMPLER_ADDRESS_MODE_CLAMP_TO_EDGE);
            samplerCreateInfo.addressModeV(VK_SAMPLER_ADDRESS_MODE_CLAMP_TO_EDGE);
            samplerCreateInfo.addressModeW(VK_SAMPLER_ADDRESS_MODE_CLAMP_TO_EDGE);
            samplerCreateInfo.maxAnisotropy(1);
            samplerCreateInfo.borderColor(VK_BORDER_COLOR_FLOAT_OPAQUE_WHITE);
            samplerCreateInfo.mipmapMode(VK_SAMPLER_MIPMAP_MODE_LINEAR);
            samplerCreateInfo.minLod(0); // Optional
            samplerCreateInfo.maxLod(1);
            samplerCreateInfo.mipLodBias(0); // Optional

            LongBuffer pImageSampler = stack.mallocLong(1);
            int result = vkCreateSampler(application.getLogicalDevice(), samplerCreateInfo, null, pImageSampler);
            if (result != VK_SUCCESS) {
                throw new RuntimeException("Failed to create image sampler (result = " + result + ")");
            }
            long imageSampler = pImageSampler.get(0);

            // Will later be true because of the specified attachment transition after renderpass
            int finalLayout = VK_IMAGE_LAYOUT_SHADER_READ_ONLY_OPTIMAL;
            texture.set(image, imageAllocation, imageView, finalLayout, imageSampler);
        }
    }

    protected void initMultisampledColorTexture(Texture texture) {
        try (MemoryStack stack = stackPush()) {
            int imageFormat = application.getSwapChain().getImageFormat();

            LongBuffer pColorImage = stack.mallocLong(1);
            PointerBuffer pColorImageAllocation = stack.mallocPointer(1);
            application.getImageManager().createImage(
                extent.width(),
                extent.height(),
                1,
                application.getMsaaSamples(),
                imageFormat,
                VK_IMAGE_USAGE_COLOR_ATTACHMENT_BIT | VK_IMAGE_USAGE_TRANSIENT_ATTACHMENT_BIT,
                VMA_MEMORY_USAGE_AUTO_PREFER_DEVICE,
                1,
                pColorImage,
                pColorImageAllocation
            );
            long image = pColorImage.get(0);
            long imageAllocation = pColorImageAllocation.get(0);

            long imageView = application.getImageManager().createImageView(image, imageFormat, VK_IMAGE_ASPECT_COLOR_BIT, 1);

            // Will later be true because of the specified attachment transition after renderpass
            int finalLayout = VK_IMAGE_LAYOUT_COLOR_ATTACHMENT_OPTIMAL;
            texture.set(image, imageAllocation, imageView, finalLayout);
        }
    }

    protected void initDepthTexture(Texture texture) {
        try (MemoryStack stack = stackPush()) {
            int depthFormat = findDepthFormat(stack);

            LongBuffer pDepthImage = stack.mallocLong(1);
            PointerBuffer pDepthImageAllocation = stack.mallocPointer(1);
            application.getImageManager().createImage(
                extent.width(),
                extent.height(),
                1,
                VK_SAMPLE_COUNT_1_BIT,
                depthFormat,
                VK_IMAGE_USAGE_DEPTH_STENCIL_ATTACHMENT_BIT | VK_IMAGE_USAGE_SAMPLED_BIT,
                VMA_MEMORY_USAGE_AUTO_PREFER_DEVICE,
                1,
                pDepthImage,
                pDepthImageAllocation
            );

            long image = pDepthImage.get(0);
            long imageAllocation = pDepthImageAllocation.get(0);

            long imageView = application.getImageManager().createImageView(image, depthFormat, VK_IMAGE_ASPECT_DEPTH_BIT, 1);

            VkSamplerCreateInfo samplerCreateInfo = VkSamplerCreateInfo.callocStack(stack);
            samplerCreateInfo.sType(VK_STRUCTURE_TYPE_SAMPLER_CREATE_INFO);
            samplerCreateInfo.magFilter(VK_FILTER_LINEAR);
            samplerCreateInfo.minFilter(VK_FILTER_LINEAR);
            samplerCreateInfo.addressModeU(VK_SAMPLER_ADDRESS_MODE_CLAMP_TO_EDGE);
            samplerCreateInfo.addressModeV(VK_SAMPLER_ADDRESS_MODE_CLAMP_TO_EDGE);
            samplerCreateInfo.addressModeW(VK_SAMPLER_ADDRESS_MODE_CLAMP_TO_EDGE);
            samplerCreateInfo.maxAnisotropy(1);
            samplerCreateInfo.borderColor(VK_BORDER_COLOR_FLOAT_OPAQUE_WHITE);
            samplerCreateInfo.mipmapMode(VK_SAMPLER_MIPMAP_MODE_LINEAR);
            samplerCreateInfo.minLod(0); // Optional
            samplerCreateInfo.maxLod(1);
            samplerCreateInfo.mipLodBias(0); // Optional

            LongBuffer pImageSampler = stack.mallocLong(1);
            int result = vkCreateSampler(application.getLogicalDevice(), samplerCreateInfo, null, pImageSampler);
            if (result != VK_SUCCESS) {
                throw new RuntimeException("Failed to create image sampler (result = " + result + ")");
            }
            long imageSampler = pImageSampler.get(0);

            // Will later be true because of the specified attachment transition after renderpass
            int finalLayout = VK_IMAGE_LAYOUT_DEPTH_STENCIL_READ_ONLY_OPTIMAL;
            texture.set(image, imageAllocation, imageView, finalLayout, imageSampler);
        }
    }

    protected void initMultisampledDepthTexture(Texture texture) {
        try (MemoryStack stack = stackPush()) {
            int depthFormat = findDepthFormat(stack);

            LongBuffer pDepthImage = stack.mallocLong(1);
            PointerBuffer pDepthImageAllocation = stack.mallocPointer(1);
            application.getImageManager().createImage(
                extent.width(),
                extent.height(),
                1,
                application.getMsaaSamples(),
                depthFormat,
                VK_IMAGE_USAGE_DEPTH_STENCIL_ATTACHMENT_BIT | VK_IMAGE_USAGE_TRANSIENT_ATTACHMENT_BIT,
                VMA_MEMORY_USAGE_AUTO_PREFER_DEVICE,
                1,
                pDepthImage,
                pDepthImageAllocation
            );

            long image = pDepthImage.get(0);
            long imageAllocation = pDepthImageAllocation.get(0);

            long imageView = application.getImageManager().createImageView(image, depthFormat, VK_IMAGE_ASPECT_DEPTH_BIT, 1);

            // Will later be true because of the specified attachment transition after renderpass
            int finalLayout = VK_IMAGE_LAYOUT_DEPTH_STENCIL_ATTACHMENT_OPTIMAL;
            texture.set(image, imageAllocation, imageView, finalLayout);
        }
    }

    protected int findDepthFormat(MemoryStack stack) {
        return findSupportedFormat(
            stack.ints(VK_FORMAT_D32_SFLOAT, VK_FORMAT_D32_SFLOAT_S8_UINT, VK_FORMAT_D24_UNORM_S8_UINT),
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
}
