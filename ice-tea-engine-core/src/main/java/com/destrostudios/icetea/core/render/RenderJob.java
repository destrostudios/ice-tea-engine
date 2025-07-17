package com.destrostudios.icetea.core.render;

import com.destrostudios.icetea.core.object.NativeObject;
import com.destrostudios.icetea.core.texture.Texture;
import lombok.Getter;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.*;

import java.nio.IntBuffer;
import java.nio.LongBuffer;
import java.util.*;
import java.util.function.Function;

import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.vulkan.VK10.*;

public abstract class RenderJob<RPC extends RenderPipelineCreator<?, ?>> extends NativeObject {

    public RenderJob(String name) {
        this.name = name;
    }
    @Getter
    private String name;
    protected boolean autoBeginAndEndRenderPass = true;
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

    public void preRender(RenderRecorder recorder, MemoryStack stack) {

    }

    public void renderStart(RenderRecorder recorder, MemoryStack stack) {
        if (autoBeginAndEndRenderPass) {
            recorder.beginRenderPass(this, stack);
        }
    }

    public abstract List<RenderTask> render(MemoryStack stack);

    public void renderEnd(RenderRecorder recorder, MemoryStack stack) {
        if (autoBeginAndEndRenderPass) {
            recorder.endRenderPass();
        }
    }

    public void postRender(RenderRecorder recorder, MemoryStack stack) {

    }

    public void afterAllRenderJobsUpdatedNative() {

    }

    @Override
    protected void cleanupNativeInternal() {
        frameBuffers.forEach(frameBuffer -> vkDestroyFramebuffer(application.getLogicalDevice(), frameBuffer, null));
        vkDestroyRenderPass(application.getLogicalDevice(), renderPass, null);
        super.cleanupNativeInternal();
    }

    // Helper methods

    protected void initColorTexture(Texture texture, MemoryStack stack) {
        initColorTexture(
            texture,
            application.getSwapChain().getImageFormat(),
            1,
            VK_IMAGE_USAGE_COLOR_ATTACHMENT_BIT | VK_IMAGE_USAGE_SAMPLED_BIT,
            true,
            stack
        );
    }

    protected void initColorTexture(Texture texture, int format, int mipLevels, int usage, boolean createSampler, MemoryStack stack) {
        initColorTexture(
            texture,
            format,
            1,
            mipLevels,
            usage,
            0,
            VK_IMAGE_VIEW_TYPE_2D,
            createSampler,
            stack
        );
    }

    protected void initCubeMapTexture(Texture texture, int format, int mipLevels, int usage, MemoryStack stack) {
        initColorTexture(
            texture,
            format,
            6,
            mipLevels,
            usage,
            VK_IMAGE_CREATE_CUBE_COMPATIBLE_BIT,
            VK_IMAGE_VIEW_TYPE_CUBE,
            true,
            stack
        );
    }

    protected void initColorTexture(Texture texture, int format, int layers, int mipLevels, int usage, int flags, int viewType, boolean createSampler, MemoryStack stack) {
        texture.set(
            VK_IMAGE_ASPECT_COLOR_BIT,
            format,
            layers,
            mipLevels,
            VK_SAMPLE_COUNT_1_BIT,
            usage
        );
        texture.setWidth(extent.width());
        texture.setHeight(extent.height());
        texture.updateNative(application);
        texture.createImage(flags, stack);
        texture.createImageView(viewType, stack);
        if (createSampler) {
            texture.createSampler(
                VK_SAMPLER_ADDRESS_MODE_CLAMP_TO_EDGE,
                null,
                VK_BORDER_COLOR_FLOAT_OPAQUE_WHITE,
                VK_SAMPLER_MIPMAP_MODE_LINEAR,
                stack
            );
        }
    }

    protected void initMultisampledColorTexture(Texture texture, MemoryStack stack) {
        texture.set(
            VK_IMAGE_ASPECT_COLOR_BIT,
            application.getSwapChain().getImageFormat(),
            1,
            1,
            application.getMsaaSamples(),
            VK_IMAGE_USAGE_COLOR_ATTACHMENT_BIT | VK_IMAGE_USAGE_TRANSIENT_ATTACHMENT_BIT
        );
        texture.setWidth(extent.width());
        texture.setHeight(extent.height());
        texture.updateNative(application);
        texture.createImage(stack);
        texture.createImageView(stack);
    }

    protected void initDepthTexture(Texture texture, MemoryStack stack) {
        texture.set(
            VK_IMAGE_ASPECT_DEPTH_BIT,
            findDepthFormat(stack),
            VK_IMAGE_USAGE_DEPTH_STENCIL_ATTACHMENT_BIT | VK_IMAGE_USAGE_SAMPLED_BIT
        );
        texture.setWidth(extent.width());
        texture.setHeight(extent.height());
        texture.updateNative(application);
        texture.createImage(stack);
        texture.createImageView(stack);
        texture.createSampler(
            VK_SAMPLER_ADDRESS_MODE_CLAMP_TO_EDGE,
            null,
            VK_BORDER_COLOR_FLOAT_OPAQUE_WHITE,
            VK_SAMPLER_MIPMAP_MODE_LINEAR,
            stack
        );
    }

    protected void initMultisampledDepthTexture(Texture texture, MemoryStack stack) {
        texture.set(
            VK_IMAGE_ASPECT_DEPTH_BIT,
            findDepthFormat(stack),
            1,
            1,
            application.getMsaaSamples(),
            VK_IMAGE_USAGE_DEPTH_STENCIL_ATTACHMENT_BIT | VK_IMAGE_USAGE_TRANSIENT_ATTACHMENT_BIT
        );
        texture.setWidth(extent.width());
        texture.setHeight(extent.height());
        texture.updateNative(application);
        texture.createImage(stack);
        texture.createImageView(stack);
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
