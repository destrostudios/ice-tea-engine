package com.destrostudios.icetea.core.render.scene;

import com.destrostudios.icetea.core.Application;
import com.destrostudios.icetea.core.scene.Geometry;
import com.destrostudios.icetea.core.render.GeometryRenderContext;
import com.destrostudios.icetea.core.texture.Texture;
import com.destrostudios.icetea.core.render.RenderJob;
import com.destrostudios.icetea.core.render.RenderPipeline;
import lombok.Getter;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.*;

import java.nio.IntBuffer;
import java.nio.LongBuffer;

import static org.lwjgl.system.MemoryStack.stackGet;
import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.vulkan.KHRCreateRenderpass2.vkCreateRenderPass2KHR;
import static org.lwjgl.vulkan.KHRDepthStencilResolve.*;
import static org.lwjgl.vulkan.KHRSwapchain.VK_IMAGE_LAYOUT_PRESENT_SRC_KHR;
import static org.lwjgl.vulkan.VK10.*;

public class SceneRenderJob extends RenderJob<SceneGeometryRenderContext> {

    @Getter
    private Texture multisampledColorTexture;
    @Getter
    private Texture multisampledDepthTexture;
    @Getter
    private Texture resolvedDepthTexture;

    @Override
    public void init(Application application) {
        super.init(application);
        initRenderPass();
        multisampledColorTexture = createMultisampledColorTexture();
        initMultisampledDepthTexture();
        initResolvedDepthTexture();
        initFrameBuffers();
    }

    @Override
    protected VkExtent2D calculateExtent() {
        return application.getSwapChain().getExtent();
    }

    private void initRenderPass() {
        try (MemoryStack stack = stackPush()) {
            VkAttachmentDescription2KHR.Buffer attachments = VkAttachmentDescription2KHR.callocStack(4, stack);
            VkAttachmentReference2KHR.Buffer attachmentRefs = VkAttachmentReference2KHR.callocStack(4, stack);

            int colorFormat = getSwapChainImageFormat();
            int depthFormat = findDepthFormat();

            // Color attachment (Multisampled)

            VkAttachmentDescription2KHR multisampledColorAttachment = attachments.get(0);
            multisampledColorAttachment.format(colorFormat);
            multisampledColorAttachment.samples(application.getMsaaSamples());
            multisampledColorAttachment.loadOp(VK_ATTACHMENT_LOAD_OP_CLEAR);
            multisampledColorAttachment.storeOp(VK_ATTACHMENT_STORE_OP_DONT_CARE);
            multisampledColorAttachment.stencilLoadOp(VK_ATTACHMENT_LOAD_OP_DONT_CARE);
            multisampledColorAttachment.stencilStoreOp(VK_ATTACHMENT_STORE_OP_DONT_CARE);
            multisampledColorAttachment.initialLayout(VK_IMAGE_LAYOUT_UNDEFINED);
            multisampledColorAttachment.finalLayout(VK_IMAGE_LAYOUT_COLOR_ATTACHMENT_OPTIMAL);

            VkAttachmentReference2KHR multisampledColorAttachmentRef = attachmentRefs.get(0);
            multisampledColorAttachmentRef.attachment(0);
            multisampledColorAttachmentRef.layout(VK_IMAGE_LAYOUT_COLOR_ATTACHMENT_OPTIMAL);

            // Depth-Stencil attachment (Multisampled)

            VkAttachmentDescription2KHR multisampledDepthAttachment = attachments.get(1);
            multisampledDepthAttachment.format(depthFormat);
            multisampledDepthAttachment.samples(application.getMsaaSamples());
            multisampledDepthAttachment.loadOp(VK_ATTACHMENT_LOAD_OP_CLEAR);
            multisampledDepthAttachment.storeOp(VK_ATTACHMENT_STORE_OP_DONT_CARE);
            multisampledDepthAttachment.stencilLoadOp(VK_ATTACHMENT_LOAD_OP_DONT_CARE);
            multisampledDepthAttachment.stencilStoreOp(VK_ATTACHMENT_STORE_OP_DONT_CARE);
            multisampledDepthAttachment.initialLayout(VK_IMAGE_LAYOUT_UNDEFINED);
            multisampledDepthAttachment.finalLayout(VK_IMAGE_LAYOUT_DEPTH_STENCIL_ATTACHMENT_OPTIMAL);

            VkAttachmentReference2KHR multisampledDepthAttachmentRef = attachmentRefs.get(1);
            multisampledDepthAttachmentRef.attachment(1);
            multisampledDepthAttachmentRef.layout(VK_IMAGE_LAYOUT_DEPTH_STENCIL_ATTACHMENT_OPTIMAL);

            // Color attachment (Resolved)

            VkAttachmentDescription2KHR resolvedColorAttachment = attachments.get(2);
            resolvedColorAttachment.format(colorFormat);
            resolvedColorAttachment.samples(VK_SAMPLE_COUNT_1_BIT);
            resolvedColorAttachment.loadOp(VK_ATTACHMENT_LOAD_OP_DONT_CARE);
            resolvedColorAttachment.storeOp(VK_ATTACHMENT_STORE_OP_STORE);
            resolvedColorAttachment.stencilLoadOp(VK_ATTACHMENT_LOAD_OP_DONT_CARE);
            resolvedColorAttachment.stencilStoreOp(VK_ATTACHMENT_STORE_OP_DONT_CARE);
            resolvedColorAttachment.initialLayout(VK_IMAGE_LAYOUT_UNDEFINED);
            resolvedColorAttachment.finalLayout(isPresentingRenderJob() ? VK_IMAGE_LAYOUT_PRESENT_SRC_KHR : VK_IMAGE_LAYOUT_COLOR_ATTACHMENT_OPTIMAL);

            VkAttachmentReference2KHR resolvedColorAttachmentRef = attachmentRefs.get(2);
            resolvedColorAttachmentRef.attachment(2);
            resolvedColorAttachmentRef.layout(VK_IMAGE_LAYOUT_COLOR_ATTACHMENT_OPTIMAL);

            // Depth-Stencil attachment (Resolved)

            VkAttachmentDescription2KHR resolvedDepthAttachment = attachments.get(3);
            resolvedDepthAttachment.format(depthFormat);
            resolvedDepthAttachment.samples(VK_SAMPLE_COUNT_1_BIT);
            resolvedDepthAttachment.loadOp(VK_ATTACHMENT_LOAD_OP_DONT_CARE);
            resolvedDepthAttachment.storeOp(VK_ATTACHMENT_STORE_OP_STORE);
            resolvedDepthAttachment.stencilLoadOp(VK_ATTACHMENT_LOAD_OP_DONT_CARE);
            resolvedDepthAttachment.stencilStoreOp(VK_ATTACHMENT_STORE_OP_DONT_CARE);
            resolvedDepthAttachment.initialLayout(VK_IMAGE_LAYOUT_UNDEFINED);
            resolvedDepthAttachment.finalLayout(VK_IMAGE_LAYOUT_DEPTH_STENCIL_ATTACHMENT_OPTIMAL);

            VkAttachmentReference2KHR resolvedDepthAttachmentRef = attachmentRefs.get(3);
            resolvedDepthAttachmentRef.attachment(3);
            resolvedDepthAttachmentRef.layout(VK_IMAGE_LAYOUT_DEPTH_STENCIL_ATTACHMENT_OPTIMAL);

            // Subpass and dependencies

            VkSubpassDescription2KHR.Buffer subpass = VkSubpassDescription2KHR.callocStack(1, stack);
            subpass.pipelineBindPoint(VK_PIPELINE_BIND_POINT_GRAPHICS);
            subpass.colorAttachmentCount(1);
            subpass.pColorAttachments(VkAttachmentReference2KHR.callocStack(1, stack).put(0, multisampledColorAttachmentRef));
            subpass.pDepthStencilAttachment(multisampledDepthAttachmentRef);
            subpass.pResolveAttachments(VkAttachmentReference2KHR.callocStack(1, stack).put(0, resolvedColorAttachmentRef));

            VkSubpassDescriptionDepthStencilResolveKHR subpassDepthStencilResolve = VkSubpassDescriptionDepthStencilResolveKHR.callocStack(stack);
            subpassDepthStencilResolve.sType(VK_STRUCTURE_TYPE_SUBPASS_DESCRIPTION_DEPTH_STENCIL_RESOLVE_KHR);
            int depthStencilResolveMode = application.getPhysicalDeviceInformation().getDepthStencilResolveMode();
            subpassDepthStencilResolve.depthResolveMode(depthStencilResolveMode);
            subpassDepthStencilResolve.stencilResolveMode(depthStencilResolveMode);
            subpassDepthStencilResolve.pDepthStencilResolveAttachment(resolvedDepthAttachmentRef);
            subpass.pNext(subpassDepthStencilResolve.address());

            VkSubpassDependency2KHR.Buffer dependency = VkSubpassDependency2KHR.callocStack(1, stack);
            dependency.srcSubpass(VK_SUBPASS_EXTERNAL);
            dependency.dstSubpass(0);
            dependency.srcStageMask(VK_PIPELINE_STAGE_COLOR_ATTACHMENT_OUTPUT_BIT);
            dependency.srcAccessMask(0);
            dependency.dstStageMask(VK_PIPELINE_STAGE_COLOR_ATTACHMENT_OUTPUT_BIT);
            dependency.dstAccessMask(VK_ACCESS_COLOR_ATTACHMENT_READ_BIT | VK_ACCESS_COLOR_ATTACHMENT_WRITE_BIT);

            VkRenderPassCreateInfo2KHR renderPassCreateInfo = VkRenderPassCreateInfo2KHR.callocStack(stack);
            renderPassCreateInfo.sType(VK_STRUCTURE_TYPE_RENDER_PASS_CREATE_INFO);
            renderPassCreateInfo.pAttachments(attachments);
            renderPassCreateInfo.pSubpasses(subpass);
            renderPassCreateInfo.pDependencies(dependency);

            LongBuffer pRenderPass = stack.mallocLong(1);
            if (vkCreateRenderPass2KHR(application.getLogicalDevice(), renderPassCreateInfo, null, pRenderPass) != VK_SUCCESS) {
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

    private int getSwapChainImageFormat() {
        return application.getSwapChain().getImageFormat();
    }

    private void initMultisampledDepthTexture() {
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
                VK_IMAGE_USAGE_DEPTH_STENCIL_ATTACHMENT_BIT | VK_IMAGE_USAGE_TRANSIENT_ATTACHMENT_BIT,
                VK_MEMORY_PROPERTY_DEVICE_LOCAL_BIT,
                pDepthImage,
                pDepthImageMemory
            );

            long image = pDepthImage.get(0);
            long imageMemory = pDepthImageMemory.get(0);

            application.getImageManager().transitionImageLayout(image, depthFormat, VK_IMAGE_LAYOUT_UNDEFINED, VK_IMAGE_LAYOUT_DEPTH_STENCIL_ATTACHMENT_OPTIMAL, 1);

            long imageView = application.getImageManager().createImageView(image, depthFormat, VK_IMAGE_ASPECT_DEPTH_BIT, 1);

            multisampledDepthTexture = new Texture(image, imageMemory, imageView);
            multisampledDepthTexture.init(application);
        }
    }

    private void initResolvedDepthTexture() {
        try (MemoryStack stack = stackPush()) {
            int depthFormat = findDepthFormat();

            LongBuffer pDepthImage = stack.mallocLong(1);
            LongBuffer pDepthImageMemory = stack.mallocLong(1);
            application.getImageManager().createImage(
                extent.width(),
                extent.height(),
                1,
                VK_SAMPLE_COUNT_1_BIT,
                depthFormat,
                VK_IMAGE_TILING_OPTIMAL,
                VK_IMAGE_USAGE_DEPTH_STENCIL_ATTACHMENT_BIT | VK_IMAGE_USAGE_SAMPLED_BIT,
                VK_MEMORY_PROPERTY_DEVICE_LOCAL_BIT,
                pDepthImage,
                pDepthImageMemory
            );

            long image = pDepthImage.get(0);
            long imageMemory = pDepthImageMemory.get(0);

            application.getImageManager().transitionImageLayout(image, depthFormat, VK_IMAGE_LAYOUT_UNDEFINED, VK_IMAGE_LAYOUT_DEPTH_STENCIL_ATTACHMENT_OPTIMAL, 1);

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
            if (vkCreateSampler(application.getLogicalDevice(), samplerCreateInfo, null, pImageSampler) != VK_SUCCESS) {
                throw new RuntimeException("Failed to create image sampler");
            }
            long imageSampler = pImageSampler.get(0);

            resolvedDepthTexture = new Texture(image, imageMemory, imageView, imageSampler);
            resolvedDepthTexture.init(application);
        }
    }

    private void initFrameBuffers() {
        initFrameBuffers(frameBufferIndex -> new long[] {
            multisampledColorTexture.getImageView(),
            multisampledDepthTexture.getImageView(),
            getResolvedColorImageView(frameBufferIndex),
            resolvedDepthTexture.getImageView()
        });
    }

    @Override
    public boolean isRendering(Geometry geometry) {
        return true;
    }

    @Override
    public SceneGeometryRenderContext createGeometryRenderContext() {
        return new SceneGeometryRenderContext(() -> application.getSceneCamera(), application.getBucketRenderer());
    }

    @Override
    public VkClearValue.Buffer getClearValues(MemoryStack stack) {
        VkClearValue.Buffer clearValues = VkClearValue.callocStack(2, stack);
        clearValues.get(0).color().float32(stack.floats(0, 0, 0, 1));
        clearValues.get(1).depthStencil().set(1, 0);
        return clearValues;
    }

    @Override
    public void render(VkCommandBuffer commandBuffer, int commandBufferIndex, MemoryStack stack) {
        application.getBucketRenderer().render(application.getRootNode(), geometry -> {
            GeometryRenderContext<?> geometryRenderContext = geometry.getRenderContext(this);
            if (geometryRenderContext != null) {
                RenderPipeline<?> renderPipeline = geometryRenderContext.getRenderPipeline();
                vkCmdBindPipeline(commandBuffer, VK_PIPELINE_BIND_POINT_GRAPHICS, renderPipeline.getPipeline());
                LongBuffer vertexBuffers = stack.longs(geometry.getMesh().getVertexBuffer());
                LongBuffer offsets = stack.longs(0);
                vkCmdBindVertexBuffers(commandBuffer, 0, vertexBuffers, offsets);
                if (geometry.getMesh().getIndexBuffer() != null) {
                    vkCmdBindIndexBuffer(commandBuffer, geometry.getMesh().getIndexBuffer(), 0, VK_INDEX_TYPE_UINT32);
                }
                vkCmdBindDescriptorSets(commandBuffer, VK_PIPELINE_BIND_POINT_GRAPHICS, renderPipeline.getPipelineLayout(), 0, stack.longs(geometryRenderContext.getDescriptorSet(commandBufferIndex)), null);
                if (geometry.getMesh().getIndices() != null) {
                    vkCmdDrawIndexed(commandBuffer, geometry.getMesh().getIndices().length, 1, 0, 0, 0);
                } else {
                    vkCmdDraw(commandBuffer, geometry.getMesh().getVertices().length, 1, 0, 0);
                }
            }
        });
    }

    @Override
    public void cleanup() {
        if (isInitialized()) {
            multisampledColorTexture.cleanup();
            multisampledDepthTexture.cleanup();
            resolvedDepthTexture.cleanup();
        }
        super.cleanup();
    }
}
