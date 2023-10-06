package com.destrostudios.icetea.core.render;

import com.destrostudios.icetea.core.object.NativeObject;
import com.destrostudios.icetea.core.resource.descriptor.SimpleTextureDescriptor;
import com.destrostudios.icetea.core.scene.Geometry;
import com.destrostudios.icetea.core.texture.Texture;
import lombok.Getter;
import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.*;

import java.nio.LongBuffer;
import java.util.*;
import java.util.function.Function;

import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.util.vma.Vma.*;
import static org.lwjgl.vulkan.VK10.*;

public abstract class RenderJob<GRC extends GeometryRenderContext<?, ?>> extends NativeObject {

    @Getter
    protected VkExtent2D extent;
    @Getter
    protected long renderPass;
    private HashMap<Geometry, GRC> renderContexts = new HashMap<>();
    @Getter
    private Texture resolvedColorTexture;
    protected List<Long> frameBuffers;

    @Override
    protected void initNative() {
        super.initNative();
        extent = calculateExtent();
    }

    protected abstract VkExtent2D calculateExtent();

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

    protected long getResolvedColorImageView(int frameBufferIndex) {
        if (isPresentingRenderJob()) {
            return application.getSwapChain().getImageViews().get(frameBufferIndex);
        } else {
            if (resolvedColorTexture == null) {
                resolvedColorTexture = new Texture();
                resolvedColorTexture.setDescriptor("default", new SimpleTextureDescriptor());
            }
            if (resolvedColorTexture.getImage() == null) {
                initResolvedColorTexture(resolvedColorTexture);
            }
            return resolvedColorTexture.getImageView();
        }
    }

    private void initResolvedColorTexture(Texture texture) {
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

    public abstract VkClearValue.Buffer getClearValues(MemoryStack stack);

    public abstract List<RenderTask> render();

    @Override
    public void updateNative() {
        super.updateNative();
        if (resolvedColorTexture != null) {
            resolvedColorTexture.updateNative(application);
        }
        synchronizeRenderContextsWithRootNode();
    }

    private void synchronizeRenderContextsWithRootNode() {
        // TODO: Performance can be improved here
        for (Map.Entry<Geometry, GRC> entry : renderContexts.entrySet().toArray(Map.Entry[]::new)) {
            Geometry geometry = entry.getKey();
            if ((!geometry.hasParent(application.getRootNode())) || (!isRendering(geometry))) {
                entry.getValue().cleanupNative();
                renderContexts.remove(geometry);
            }
        }
        application.getRootNode().forEachGeometry(geometry -> {
            GRC renderContext = getRenderContext(geometry);
            if ((renderContext == null) && isRendering(geometry)) {
                renderContext = createGeometryRenderContext(geometry);
                renderContexts.put(geometry, renderContext);
            }
        });
    }

    public void updateRenderContextsNative() {
        for (GRC renderContext : renderContexts.values()) {
            renderContext.updateNative(application);
        }
    }

    protected abstract boolean isRendering(Geometry geometry);

    protected abstract GRC createGeometryRenderContext(Geometry geometry);

    @Override
    protected void cleanupNativeInternal() {
        for (GRC renderContext : renderContexts.values()) {
            renderContext.cleanupNative();
        }
        if (resolvedColorTexture != null) {
            resolvedColorTexture.cleanupNative();
        }
        frameBuffers.forEach(frameBuffer -> vkDestroyFramebuffer(application.getLogicalDevice(), frameBuffer, null));
        vkDestroyRenderPass(application.getLogicalDevice(), renderPass, null);
        super.cleanupNativeInternal();
    }

    protected GRC getRenderContext(Geometry geometry) {
        return renderContexts.get(geometry);
    }
}
