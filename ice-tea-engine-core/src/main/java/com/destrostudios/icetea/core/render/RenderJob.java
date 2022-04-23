package com.destrostudios.icetea.core.render;

import com.destrostudios.icetea.core.lifecycle.LifecycleObject;
import com.destrostudios.icetea.core.scene.Geometry;
import com.destrostudios.icetea.core.texture.Texture;
import lombok.Getter;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.*;

import java.nio.LongBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.vulkan.VK10.*;

public abstract class RenderJob<GRC extends GeometryRenderContext<?>> extends LifecycleObject {

    @Getter
    protected VkExtent2D extent;
    @Getter
    protected long renderPass;
    @Getter
    private Texture resolvedColorTexture;
    protected List<Long> frameBuffers;

    @Override
    protected void init() {
        super.init();
        extent = calculateExtent();
    }

    protected abstract VkExtent2D calculateExtent();

    protected Texture createMultisampledColorTexture() {
        try (MemoryStack stack = stackPush()) {
            int imageFormat = application.getSwapChain().getImageFormat();

            LongBuffer pColorImage = stack.mallocLong(1);
            LongBuffer pColorImageMemory = stack.mallocLong(1);
            application.getImageManager().createImage(
                extent.width(),
                extent.height(),
                1,
                application.getMsaaSamples(),
                imageFormat,
                VK_IMAGE_TILING_OPTIMAL,
                VK_IMAGE_USAGE_COLOR_ATTACHMENT_BIT | VK_IMAGE_USAGE_TRANSIENT_ATTACHMENT_BIT,
                VK_MEMORY_PROPERTY_DEVICE_LOCAL_BIT,
                pColorImage,
                pColorImageMemory
            );
            long image = pColorImage.get(0);
            long imageMemory = pColorImageMemory.get(0);

            int finalLayout = VK_IMAGE_LAYOUT_COLOR_ATTACHMENT_OPTIMAL;
            application.getImageManager().transitionImageLayout(image, imageFormat, VK_IMAGE_LAYOUT_UNDEFINED, finalLayout, 1);

            long imageView = application.getImageManager().createImageView(image, imageFormat, VK_IMAGE_ASPECT_COLOR_BIT, 1);

            return new Texture(image, imageMemory, imageView, finalLayout);
        }
    }

    protected long getResolvedColorImageView(int frameBufferIndex) {
        if (isPresentingRenderJob()) {
            return application.getSwapChain().getImageViews().get(frameBufferIndex);
        } else {
            if (resolvedColorTexture == null) {
                resolvedColorTexture = createResolvedColorTexture();
            }
            return resolvedColorTexture.getImageView();
        }
    }

    private Texture createResolvedColorTexture() {
        try (MemoryStack stack = stackPush()) {
            int imageFormat = application.getSwapChain().getImageFormat();

            LongBuffer pColorImage = stack.mallocLong(1);
            LongBuffer pColorImageMemory = stack.mallocLong(1);
            application.getImageManager().createImage(
                extent.width(),
                extent.height(),
                1,
                VK_SAMPLE_COUNT_1_BIT,
                imageFormat,
                VK_IMAGE_TILING_OPTIMAL,
                VK_IMAGE_USAGE_COLOR_ATTACHMENT_BIT | VK_IMAGE_USAGE_SAMPLED_BIT,
                VK_MEMORY_PROPERTY_DEVICE_LOCAL_BIT,
                pColorImage,
                pColorImageMemory
            );
            long image = pColorImage.get(0);
            long imageMemory = pColorImageMemory.get(0);

            int finalLayout = VK_IMAGE_LAYOUT_COLOR_ATTACHMENT_OPTIMAL;
            application.getImageManager().transitionImageLayout(image, imageFormat, VK_IMAGE_LAYOUT_UNDEFINED, finalLayout, 1);

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

            return new Texture(image, imageMemory, imageView, finalLayout, imageSampler);
        }
    }

    protected void initFrameBuffers(Function<Integer, long[]> getAttachmentsByFrameBufferIndex) {
        try (MemoryStack stack = stackPush()) {
            VkFramebufferCreateInfo framebufferCreateInfo = VkFramebufferCreateInfo.callocStack(stack);
            framebufferCreateInfo.sType(VK_STRUCTURE_TYPE_FRAMEBUFFER_CREATE_INFO);
            framebufferCreateInfo.renderPass(renderPass);
            framebufferCreateInfo.width(extent.width());
            framebufferCreateInfo.height(extent.height());
            framebufferCreateInfo.layers(1);

            int frameBuffersCount = (isPresentingRenderJob() ? application.getSwapChain().getImages().size() : 1);
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

    public long getFramebuffer(int commandBufferIndex) {
        return frameBuffers.get(isPresentingRenderJob() ? commandBufferIndex : 0);
    }

    protected boolean isPresentingRenderJob() {
        return (this == application.getSwapChain().getRenderJobManager().getPresentingRenderJob());
    }

    public abstract boolean isRendering(Geometry geometry);

    public abstract GRC createGeometryRenderContext();

    public VkRect2D getRenderArea(MemoryStack stack) {
        VkRect2D renderArea = VkRect2D.callocStack(stack);
        renderArea.offset(VkOffset2D.callocStack(stack).set(0, 0));
        renderArea.extent(extent);
        return renderArea;
    }

    public abstract VkClearValue.Buffer getClearValues(MemoryStack stack);

    public abstract void render(Consumer<RenderAction> actions);

    @Override
    public void update(float tpf) {
        super.update(tpf);
        if (resolvedColorTexture != null) {
            resolvedColorTexture.update(application, tpf);
        }
    }

    @Override
    protected void cleanupInternal() {
        if (isInitialized()) {
            application.getRootNode().forEachGeometry(geometry -> {
                GeometryRenderContext<?> renderContext = geometry.getRenderContext(this);
                if (renderContext != null) {
                    renderContext.cleanupDescriptorDependencies();
                }
            });
            if (resolvedColorTexture != null) {
                resolvedColorTexture.cleanup();
                resolvedColorTexture = null;
            }
            frameBuffers.forEach(frameBuffer -> vkDestroyFramebuffer(application.getLogicalDevice(), frameBuffer, null));
            vkDestroyRenderPass(application.getLogicalDevice(), renderPass, null);
        }
        super.cleanupInternal();
    }
}
