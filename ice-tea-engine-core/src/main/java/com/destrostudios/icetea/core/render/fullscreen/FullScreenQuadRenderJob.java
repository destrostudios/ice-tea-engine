package com.destrostudios.icetea.core.render.fullscreen;

import com.destrostudios.icetea.core.Pipeline;
import com.destrostudios.icetea.core.render.GeometryRenderContext;
import com.destrostudios.icetea.core.render.RenderJob;
import com.destrostudios.icetea.core.render.RenderTask;
import com.destrostudios.icetea.core.render.scene.SceneGeometryRenderContext;
import com.destrostudios.icetea.core.resource.ResourceDescriptorSet;
import com.destrostudios.icetea.core.scene.Geometry;
import com.destrostudios.icetea.core.shader.Shader;
import com.destrostudios.icetea.core.texture.Texture;
import lombok.Getter;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.*;

import java.nio.LongBuffer;
import java.util.List;

import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.vulkan.KHRSwapchain.VK_IMAGE_LAYOUT_PRESENT_SRC_KHR;
import static org.lwjgl.vulkan.VK10.*;

public abstract class FullScreenQuadRenderJob extends RenderJob<GeometryRenderContext<?>, FullScreenQuadRenderPipelineCreator> {

    public FullScreenQuadRenderJob() {
        super("fullScreenQuad");
        multisampledColorTexture = new Texture();
    }
    @Getter
    protected ResourceDescriptorSet resourceDescriptorSet;
    private Texture multisampledColorTexture;
    private Pipeline renderPipeline;

    @Override
    protected void initNative() {
        super.initNative();
        initRenderPass();
        initResourceDescriptorSet();
        initMultisampledColorTexture(multisampledColorTexture);
        initFrameBuffers();
        renderPipelineCreator = new FullScreenQuadRenderPipelineCreator(application, this);
        initRenderPipeline();
    }

    @Override
    protected VkExtent2D calculateExtent() {
        return application.getSwapChain().getExtent();
    }

    private void initRenderPass() {
        try (MemoryStack stack = stackPush()) {
            int colorFormat = application.getSwapChain().getImageFormat();

            VkAttachmentDescription.Buffer attachments = VkAttachmentDescription.callocStack(2, stack);
            VkAttachmentReference.Buffer attachmentRefs = VkAttachmentReference.callocStack(2, stack);

            // Color attachment (Multisampled)

            VkAttachmentDescription multisampledColorAttachment = attachments.get(0);
            multisampledColorAttachment.format(colorFormat);
            multisampledColorAttachment.samples(application.getMsaaSamples());
            multisampledColorAttachment.loadOp(VK_ATTACHMENT_LOAD_OP_DONT_CARE);
            multisampledColorAttachment.storeOp(VK_ATTACHMENT_STORE_OP_DONT_CARE);
            multisampledColorAttachment.stencilLoadOp(VK_ATTACHMENT_LOAD_OP_DONT_CARE);
            multisampledColorAttachment.stencilStoreOp(VK_ATTACHMENT_STORE_OP_DONT_CARE);
            multisampledColorAttachment.initialLayout(VK_IMAGE_LAYOUT_UNDEFINED);
            multisampledColorAttachment.finalLayout(VK_IMAGE_LAYOUT_COLOR_ATTACHMENT_OPTIMAL);

            VkAttachmentReference multisampledColorAttachmentRef = attachmentRefs.get(0);
            multisampledColorAttachmentRef.attachment(0);
            multisampledColorAttachmentRef.layout(VK_IMAGE_LAYOUT_COLOR_ATTACHMENT_OPTIMAL);

            // Color attachment (Resolved)

            VkAttachmentDescription resolvedColorAttachment = attachments.get(1);
            resolvedColorAttachment.format(colorFormat);
            resolvedColorAttachment.samples(VK_SAMPLE_COUNT_1_BIT);
            resolvedColorAttachment.loadOp(VK_ATTACHMENT_LOAD_OP_DONT_CARE);
            resolvedColorAttachment.storeOp(VK_ATTACHMENT_STORE_OP_STORE);
            resolvedColorAttachment.stencilLoadOp(VK_ATTACHMENT_LOAD_OP_DONT_CARE);
            resolvedColorAttachment.stencilStoreOp(VK_ATTACHMENT_STORE_OP_DONT_CARE);
            resolvedColorAttachment.initialLayout(VK_IMAGE_LAYOUT_UNDEFINED);
            resolvedColorAttachment.finalLayout(isPresentingRenderJob() ? VK_IMAGE_LAYOUT_PRESENT_SRC_KHR : VK_IMAGE_LAYOUT_SHADER_READ_ONLY_OPTIMAL);

            VkAttachmentReference resolvedColorAttachmentRef = attachmentRefs.get(1);
            resolvedColorAttachmentRef.attachment(1);
            resolvedColorAttachmentRef.layout(VK_IMAGE_LAYOUT_COLOR_ATTACHMENT_OPTIMAL);

            VkSubpassDescription.Buffer subpass = VkSubpassDescription.callocStack(1, stack);
            subpass.pipelineBindPoint(VK_PIPELINE_BIND_POINT_GRAPHICS);
            subpass.colorAttachmentCount(1);
            subpass.pColorAttachments(VkAttachmentReference.callocStack(1, stack).put(0, multisampledColorAttachmentRef));
            subpass.pResolveAttachments(VkAttachmentReference.callocStack(1, stack).put(0, resolvedColorAttachmentRef));

            VkRenderPassCreateInfo renderPassCreateInfo = VkRenderPassCreateInfo.callocStack(stack);
            renderPassCreateInfo.sType(VK_STRUCTURE_TYPE_RENDER_PASS_CREATE_INFO);
            renderPassCreateInfo.pAttachments(attachments);
            renderPassCreateInfo.pSubpasses(subpass);

            LongBuffer pRenderPass = stack.mallocLong(1);
            int result = vkCreateRenderPass(application.getLogicalDevice(), renderPassCreateInfo, null, pRenderPass);
            if (result != VK_SUCCESS) {
                throw new RuntimeException("Failed to create render pass (result = " + result + ")");
            }
            renderPass = pRenderPass.get(0);
        }
    }

    protected void initResourceDescriptorSet() {
        resourceDescriptorSet = new ResourceDescriptorSet();
    }

    private void initRenderPipeline() {
        FullScreenQuadRenderPipelineState state = new FullScreenQuadRenderPipelineState("renderFullScreenQuad");
        state.setFragmentShader(getFragmentShader());
        renderPipeline = renderPipelineCreator.getOrCreatePipeline(state, resourceDescriptorSet);
    }

    public abstract Shader getFragmentShader();

    private void initFrameBuffers() {
        initFrameBuffers(frameBufferIndex -> new long[] {
            multisampledColorTexture.getImageView(),
            getResolvedColorImageView(frameBufferIndex),
        });
    }

    @Override
    public boolean isRendering(Geometry geometry) {
        return false;
    }

    @Override
    public SceneGeometryRenderContext createGeometryRenderContext(Geometry geometry) {
        throw new UnsupportedOperationException();
    }

    @Override
    public VkClearValue.Buffer getClearValues(MemoryStack stack) {
        return null;
    }

    @Override
    public List<RenderTask> render() {
        return List.of(recorder -> {
            try (MemoryStack stack = stackPush()) {
                recorder.bindPipeline(renderPipeline);
                recorder.bindDescriptorSets(resourceDescriptorSet, stack);
                vkCmdDraw(recorder.getCommandBuffer(), 3, 1, 0, 0);
            }
        });
    }

    @Override
    public void updateNative() {
        super.updateNative();
        multisampledColorTexture.updateNative(application);
        renderPipeline.updateNative(application);
    }

    @Override
    protected void cleanupNativeInternal() {
        // Don't cleanup (the potentially shared) renderPipeline to keep it in the PipelineManager cache (which owns and controls its lifetime)
        multisampledColorTexture.cleanupNative();
        super.cleanupNativeInternal();
    }
}
