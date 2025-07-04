package com.destrostudios.icetea.core.render.scene;

import com.destrostudios.icetea.core.render.GeometryRenderJob;
import com.destrostudios.icetea.core.render.RenderTask;
import com.destrostudios.icetea.core.resource.descriptor.SimpleTextureDescriptor;
import com.destrostudios.icetea.core.scene.Geometry;
import com.destrostudios.icetea.core.texture.Texture;
import lombok.Getter;
import org.joml.Vector4f;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.*;

import java.nio.LongBuffer;
import java.util.List;
import java.util.stream.Collectors;

import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.vulkan.KHRCreateRenderpass2.vkCreateRenderPass2KHR;
import static org.lwjgl.vulkan.KHRDepthStencilResolve.VK_STRUCTURE_TYPE_SUBPASS_DESCRIPTION_DEPTH_STENCIL_RESOLVE_KHR;
import static org.lwjgl.vulkan.KHRSwapchain.VK_IMAGE_LAYOUT_PRESENT_SRC_KHR;
import static org.lwjgl.vulkan.VK10.*;
import static org.lwjgl.vulkan.VK12.*;

public class SceneRenderJob extends GeometryRenderJob<SceneGeometryRenderContext, SceneRenderPipelineCreator> {

    public SceneRenderJob() {
        super("scene");
        multisampledColorTexture = new Texture();
        multisampledDepthTexture = new Texture();
        resolvedColorTexture = new Texture();
        resolvedColorTexture.setDescriptor("default", new SimpleTextureDescriptor());
        resolvedDepthTexture = new Texture();
        resolvedDepthTexture.setDescriptor("default", new SimpleTextureDescriptor());
    }
    private Texture multisampledColorTexture;
    private Texture multisampledDepthTexture;
    @Getter
    private Texture resolvedColorTexture;
    @Getter
    private Texture resolvedDepthTexture;

    @Override
    protected void initNative() {
        super.initNative();
        initRenderPass();
        initMultisampledColorTexture(multisampledColorTexture);
        initMultisampledDepthTexture(multisampledDepthTexture);
        initColorTexture(resolvedColorTexture);
        initDepthTexture(resolvedDepthTexture);
        initFrameBuffers();
        renderPipelineCreator = new SceneRenderPipelineCreator(application, this);
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
            int depthFormat = findDepthFormat(stack);

            // Color attachment (Multisampled)

            VkAttachmentDescription2 multisampledColorAttachment = attachments.get(0);
            multisampledColorAttachment.sType(VK_STRUCTURE_TYPE_ATTACHMENT_DESCRIPTION_2);
            multisampledColorAttachment.format(colorFormat);
            multisampledColorAttachment.samples(application.getMsaaSamples());
            multisampledColorAttachment.loadOp(VK_ATTACHMENT_LOAD_OP_CLEAR);
            multisampledColorAttachment.storeOp(VK_ATTACHMENT_STORE_OP_DONT_CARE);
            multisampledColorAttachment.stencilLoadOp(VK_ATTACHMENT_LOAD_OP_DONT_CARE);
            multisampledColorAttachment.stencilStoreOp(VK_ATTACHMENT_STORE_OP_DONT_CARE);
            multisampledColorAttachment.initialLayout(VK_IMAGE_LAYOUT_UNDEFINED);
            multisampledColorAttachment.finalLayout(VK_IMAGE_LAYOUT_COLOR_ATTACHMENT_OPTIMAL);

            VkAttachmentReference2 multisampledColorAttachmentRef = attachmentRefs.get(0);
            multisampledColorAttachmentRef.sType(VK_STRUCTURE_TYPE_ATTACHMENT_REFERENCE_2);
            multisampledColorAttachmentRef.attachment(0);
            multisampledColorAttachmentRef.layout(VK_IMAGE_LAYOUT_COLOR_ATTACHMENT_OPTIMAL);

            // Depth-Stencil attachment (Multisampled)

            VkAttachmentDescription2 multisampledDepthAttachment = attachments.get(1);
            multisampledDepthAttachment.sType(VK_STRUCTURE_TYPE_ATTACHMENT_DESCRIPTION_2);
            multisampledDepthAttachment.format(depthFormat);
            multisampledDepthAttachment.samples(application.getMsaaSamples());
            multisampledDepthAttachment.loadOp(VK_ATTACHMENT_LOAD_OP_CLEAR);
            multisampledDepthAttachment.storeOp(VK_ATTACHMENT_STORE_OP_DONT_CARE);
            multisampledDepthAttachment.stencilLoadOp(VK_ATTACHMENT_LOAD_OP_DONT_CARE);
            multisampledDepthAttachment.stencilStoreOp(VK_ATTACHMENT_STORE_OP_DONT_CARE);
            multisampledDepthAttachment.initialLayout(VK_IMAGE_LAYOUT_UNDEFINED);
            multisampledDepthAttachment.finalLayout(VK_IMAGE_LAYOUT_DEPTH_STENCIL_ATTACHMENT_OPTIMAL);

            VkAttachmentReference2 multisampledDepthAttachmentRef = attachmentRefs.get(1);
            multisampledDepthAttachmentRef.sType(VK_STRUCTURE_TYPE_ATTACHMENT_REFERENCE_2);
            multisampledDepthAttachmentRef.attachment(1);
            multisampledDepthAttachmentRef.layout(VK_IMAGE_LAYOUT_DEPTH_STENCIL_ATTACHMENT_OPTIMAL);

            // Color attachment (Resolved)

            VkAttachmentDescription2 resolvedColorAttachment = attachments.get(2);
            resolvedColorAttachment.sType(VK_STRUCTURE_TYPE_ATTACHMENT_DESCRIPTION_2);
            resolvedColorAttachment.format(colorFormat);
            resolvedColorAttachment.samples(VK_SAMPLE_COUNT_1_BIT);
            resolvedColorAttachment.loadOp(VK_ATTACHMENT_LOAD_OP_DONT_CARE);
            resolvedColorAttachment.storeOp(VK_ATTACHMENT_STORE_OP_STORE);
            resolvedColorAttachment.stencilLoadOp(VK_ATTACHMENT_LOAD_OP_DONT_CARE);
            resolvedColorAttachment.stencilStoreOp(VK_ATTACHMENT_STORE_OP_DONT_CARE);
            resolvedColorAttachment.initialLayout(VK_IMAGE_LAYOUT_UNDEFINED);
            resolvedColorAttachment.finalLayout(isPresentingRenderJob() ? VK_IMAGE_LAYOUT_PRESENT_SRC_KHR : VK_IMAGE_LAYOUT_SHADER_READ_ONLY_OPTIMAL);

            VkAttachmentReference2 resolvedColorAttachmentRef = attachmentRefs.get(2);
            resolvedColorAttachmentRef.sType(VK_STRUCTURE_TYPE_ATTACHMENT_REFERENCE_2);
            resolvedColorAttachmentRef.attachment(2);
            resolvedColorAttachmentRef.layout(VK_IMAGE_LAYOUT_COLOR_ATTACHMENT_OPTIMAL);

            // Depth-Stencil attachment (Resolved)

            VkAttachmentDescription2 resolvedDepthAttachment = attachments.get(3);
            resolvedDepthAttachment.sType(VK_STRUCTURE_TYPE_ATTACHMENT_DESCRIPTION_2);
            resolvedDepthAttachment.format(depthFormat);
            resolvedDepthAttachment.samples(VK_SAMPLE_COUNT_1_BIT);
            resolvedDepthAttachment.loadOp(VK_ATTACHMENT_LOAD_OP_DONT_CARE);
            resolvedDepthAttachment.storeOp(VK_ATTACHMENT_STORE_OP_STORE);
            resolvedDepthAttachment.stencilLoadOp(VK_ATTACHMENT_LOAD_OP_DONT_CARE);
            resolvedDepthAttachment.stencilStoreOp(VK_ATTACHMENT_STORE_OP_DONT_CARE);
            resolvedDepthAttachment.initialLayout(VK_IMAGE_LAYOUT_UNDEFINED);
            resolvedDepthAttachment.finalLayout(VK_IMAGE_LAYOUT_DEPTH_STENCIL_READ_ONLY_OPTIMAL);

            VkAttachmentReference2 resolvedDepthAttachmentRef = attachmentRefs.get(3);
            resolvedDepthAttachmentRef.sType(VK_STRUCTURE_TYPE_ATTACHMENT_REFERENCE_2);
            resolvedDepthAttachmentRef.attachment(3);
            resolvedDepthAttachmentRef.layout(VK_IMAGE_LAYOUT_DEPTH_STENCIL_ATTACHMENT_OPTIMAL);

            // Subpass and dependencies

            VkSubpassDescription2KHR.Buffer subpass = VkSubpassDescription2KHR.callocStack(1, stack);
            subpass.sType(VK_STRUCTURE_TYPE_SUBPASS_DESCRIPTION_2);
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
            dependency.sType(VK_STRUCTURE_TYPE_SUBPASS_DEPENDENCY_2);
            dependency.srcSubpass(VK_SUBPASS_EXTERNAL);
            dependency.dstSubpass(0);
            dependency.srcStageMask(VK_PIPELINE_STAGE_COLOR_ATTACHMENT_OUTPUT_BIT);
            dependency.srcAccessMask(0);
            dependency.dstStageMask(VK_PIPELINE_STAGE_COLOR_ATTACHMENT_OUTPUT_BIT);
            dependency.dstAccessMask(VK_ACCESS_COLOR_ATTACHMENT_READ_BIT | VK_ACCESS_COLOR_ATTACHMENT_WRITE_BIT);

            VkRenderPassCreateInfo2KHR renderPassCreateInfo = VkRenderPassCreateInfo2KHR.callocStack(stack);
            renderPassCreateInfo.sType(VK_STRUCTURE_TYPE_RENDER_PASS_CREATE_INFO_2);
            renderPassCreateInfo.pAttachments(attachments);
            renderPassCreateInfo.pSubpasses(subpass);
            renderPassCreateInfo.pDependencies(dependency);

            LongBuffer pRenderPass = stack.mallocLong(1);
            int result = vkCreateRenderPass2KHR(application.getLogicalDevice(), renderPassCreateInfo, null, pRenderPass);
            if (result != VK_SUCCESS) {
                throw new RuntimeException("Failed to create render pass (result = " + result + ")");
            }
            renderPass = pRenderPass.get(0);
        }
    }

    private int getSwapChainImageFormat() {
        return application.getSwapChain().getImageFormat();
    }

    private void initFrameBuffers() {
        initFrameBuffers(frameBufferIndex -> new long[] {
            multisampledColorTexture.getImageView(),
            multisampledDepthTexture.getImageView(),
            getPotentiallyPresentingColorImageView(resolvedColorTexture, frameBufferIndex),
            resolvedDepthTexture.getImageView()
        });
    }

    @Override
    protected boolean isRendering(Geometry geometry) {
        return true;
    }

    @Override
    protected SceneGeometryRenderContext createGeometryRenderContext(Geometry geometry) {
        return new SceneGeometryRenderContext(geometry, this, () -> application.getSceneCamera(), application.getBucketRenderer());
    }

    @Override
    public VkClearValue.Buffer getClearValues(MemoryStack stack) {
        VkClearValue.Buffer clearValues = VkClearValue.callocStack(2, stack);
        Vector4f clearColor = application.getConfig().getClearColor();
        clearValues.get(0).color().float32(stack.floats(clearColor.x(), clearColor.y(), clearColor.z(), clearColor.w()));
        clearValues.get(1).depthStencil().set(1, 0);
        return clearValues;
    }

    @Override
    public List<RenderTask> render() {
        return application.getBucketRenderer().getSplitOrderedGeometries().stream()
            .map(geometries -> (RenderTask) (recorder) -> {
                for (Geometry geometry : geometries) {
                    SceneGeometryRenderContext geometryRenderContext = getRenderContext(geometry);
                    if (geometryRenderContext != null) {
                        geometry.getRenderer().render(recorder, geometryRenderContext);
                    }
                }
            }).collect(Collectors.toList());
    }

    @Override
    public void updateNative() {
        super.updateNative();
        multisampledColorTexture.updateNative(application);
        multisampledDepthTexture.updateNative(application);
        resolvedColorTexture.updateNative(application);
        resolvedDepthTexture.updateNative(application);
    }

    @Override
    protected void cleanupNativeInternal() {
        resolvedDepthTexture.cleanupNative();
        resolvedColorTexture.cleanupNative();
        multisampledDepthTexture.cleanupNative();
        multisampledColorTexture.cleanupNative();
        super.cleanupNativeInternal();
    }
}
