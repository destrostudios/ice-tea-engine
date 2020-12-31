package com.destrostudios.icetea.core.render.fullscreen;

import com.destrostudios.icetea.core.*;
import com.destrostudios.icetea.core.material.descriptor.MaterialDescriptorSet;
import com.destrostudios.icetea.core.material.descriptor.MaterialDescriptorSetLayout;
import com.destrostudios.icetea.core.render.RenderJob;
import com.destrostudios.icetea.core.render.scene.SceneGeometryRenderContext;
import com.destrostudios.icetea.core.scene.Geometry;
import com.destrostudios.icetea.core.shader.Shader;
import lombok.Getter;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.*;

import java.nio.LongBuffer;
import java.util.List;

import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.vulkan.KHRSwapchain.VK_IMAGE_LAYOUT_PRESENT_SRC_KHR;
import static org.lwjgl.vulkan.VK10.*;

public abstract class FullScreenQuadRenderJob extends RenderJob<SceneGeometryRenderContext> {

    @Getter
    protected MaterialDescriptorSetLayout materialDescriptorSetLayout;
    @Getter
    protected MaterialDescriptorSet materialDescriptorSet;
    private long descriptorPool;
    private List<Long> descriptorSets;
    private FullScreenQuadRenderPipeline renderPipeline;
    private Texture multisampledColorTexture;

    @Override
    public void init(Application application) {
        super.init(application);
        initRenderPass();
        initMaterialDescriptors();
        initRenderPipeline();
        multisampledColorTexture = createMultisampledColorTexture();
        initFrameBuffers();
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
            multisampledColorAttachment.loadOp(VK_ATTACHMENT_LOAD_OP_CLEAR);
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
            resolvedColorAttachment.finalLayout(isPresentingRenderJob() ? VK_IMAGE_LAYOUT_PRESENT_SRC_KHR : VK_IMAGE_LAYOUT_COLOR_ATTACHMENT_OPTIMAL);

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
            if (vkCreateRenderPass(application.getLogicalDevice(), renderPassCreateInfo, null, pRenderPass) != VK_SUCCESS) {
                throw new RuntimeException("Failed to create render pass");
            }
            renderPass = pRenderPass.get(0);
        }
    }

    private void initMaterialDescriptors() {
        materialDescriptorSetLayout = new MaterialDescriptorSetLayout(application);
        materialDescriptorSet = new MaterialDescriptorSet(application, materialDescriptorSetLayout, application.getSwapChain().getImages().size());

        fillMaterialDescriptorLayoutAndSet();

        materialDescriptorSetLayout.initDescriptorSetLayout();

        descriptorPool = materialDescriptorSet.createDescriptorPool();
        descriptorSets = materialDescriptorSet.createDescriptorSets(descriptorPool);
    }

    protected abstract void fillMaterialDescriptorLayoutAndSet();

    private void initRenderPipeline() {
        renderPipeline = new FullScreenQuadRenderPipeline(application, this);
        renderPipeline.init();
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
    public SceneGeometryRenderContext createGeometryRenderContext() {
        return null;
    }

    @Override
    public VkClearValue.Buffer getClearValues(MemoryStack stack) {
        VkClearValue.Buffer clearValues = VkClearValue.callocStack(1, stack);
        clearValues.get(0).color().float32(stack.floats(0, 0, 0, 1));
        return clearValues;
    }

    @Override
    public void render(VkCommandBuffer commandBuffer, int commandBufferIndex, MemoryStack stack) {
        vkCmdBindDescriptorSets(commandBuffer, VK_PIPELINE_BIND_POINT_GRAPHICS, renderPipeline.getPipelineLayout(), 0, stack.longs(descriptorSets.get(commandBufferIndex)), null);
        vkCmdBindPipeline(commandBuffer, VK_PIPELINE_BIND_POINT_GRAPHICS, renderPipeline.getPipeline());
        vkCmdDraw(commandBuffer, 3, 1, 0, 0);
    }

    @Override
    public void cleanup() {
        if (isInitialized()) {
            multisampledColorTexture.cleanup();
            materialDescriptorSet.cleanupDescriptorSets(descriptorPool, descriptorSets);
            materialDescriptorSet.cleanupDescriptorPool(descriptorPool);
            materialDescriptorSetLayout.cleanupDescriptorSetLayout();
        }
        super.cleanup();
    }
}
