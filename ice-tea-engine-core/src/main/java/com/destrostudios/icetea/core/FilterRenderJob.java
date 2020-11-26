package com.destrostudios.icetea.core;

import com.destrostudios.icetea.core.materials.descriptors.SimpleTextureDescriptor;
import com.destrostudios.icetea.core.materials.descriptors.SimpleTextureDescriptorLayout;
import com.destrostudios.icetea.core.materials.descriptors.UniformDescriptor;
import com.destrostudios.icetea.core.materials.descriptors.UniformDescriptorLayout;
import lombok.Getter;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.*;

import java.nio.LongBuffer;
import java.util.List;

import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.vulkan.KHRSwapchain.VK_IMAGE_LAYOUT_PRESENT_SRC_KHR;
import static org.lwjgl.vulkan.VK10.*;

public class FilterRenderJob extends RenderJob<SceneGeometryRenderContext> {

    public FilterRenderJob(Filter filter) {
        this.filter = filter;
    }
    @Getter
    private Filter filter;
    @Getter
    private MaterialDescriptorSetLayout materialDescriptorSetLayout;
    @Getter
    private MaterialDescriptorSet materialDescriptorSet;
    private long descriptorPool;
    private List<Long> descriptorSets;
    private FilterRenderPipeline renderPipeline;
    private Texture multisampledColorTexture;

    @Override
    public void init(Application application) {
        super.init(application);
        filter.init(application);
        initRenderPass();
        initMaterialDescriptors();
        initRenderPipeline();
        multisampledColorTexture = createMultisampledColorTexture();
        initFrameBuffers();
    }

    @Override
    public void updateUniformBuffers(int currentImage, MemoryStack stack) {
        super.updateUniformBuffers(currentImage, stack);
        filter.updateUniformBuffers(currentImage, stack);
    }

    @Override
    protected VkExtent2D calculateExtent() {
        return application.getSwapChain().getExtent();
    }

    private void initRenderPass() {
        try (MemoryStack stack = stackPush()) {
            int colorFormat = getSwapChainImageFormat();

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
        materialDescriptorSet = new MaterialDescriptorSet(application, materialDescriptorSetLayout);

        UniformDescriptorLayout uniformDescriptorLayout = new UniformDescriptorLayout(VK_SHADER_STAGE_FRAGMENT_BIT);
        UniformDescriptor<UniformDescriptorLayout> uniformDescriptor = new UniformDescriptor<>("config", uniformDescriptorLayout, filter.getUniformData());
        materialDescriptorSetLayout.addDescriptorLayout(uniformDescriptorLayout);
        materialDescriptorSet.addDescriptor(uniformDescriptor);

        RenderJob<?> previousRenderJob = application.getSwapChain().getRenderJobManager().getPreviousRenderJob(this);
        SceneRenderJob sceneRenderJob = application.getSwapChain().getRenderJobManager().getSceneRenderJob();

        SimpleTextureDescriptorLayout colorTextureDescriptorLayout = new SimpleTextureDescriptorLayout();
        SimpleTextureDescriptor colorTextureDescriptor = new SimpleTextureDescriptor("colorMap", colorTextureDescriptorLayout, previousRenderJob.getResolvedColorTexture());
        materialDescriptorSetLayout.addDescriptorLayout(colorTextureDescriptorLayout);
        materialDescriptorSet.addDescriptor(colorTextureDescriptor);

        SimpleTextureDescriptorLayout depthTextureDescriptorLayout = new SimpleTextureDescriptorLayout();
        SimpleTextureDescriptor depthTextureDescriptor = new SimpleTextureDescriptor("depthMap", depthTextureDescriptorLayout, sceneRenderJob.getResolvedDepthTexture());
        materialDescriptorSetLayout.addDescriptorLayout(depthTextureDescriptorLayout);
        materialDescriptorSet.addDescriptor(depthTextureDescriptor);

        materialDescriptorSetLayout.initDescriptorSetLayout();

        descriptorPool = materialDescriptorSet.createDescriptorPool();
        descriptorSets = materialDescriptorSet.createDescriptorSets(descriptorPool);
    }

    private void initRenderPipeline() {
        renderPipeline = new FilterRenderPipeline(application, this);
        renderPipeline.init();
    }

    private int getSwapChainImageFormat() {
        return application.getSwapChain().getImageFormat();
    }

    private void initFrameBuffers() {
        initFrameBuffers(frameBufferIndex -> new long[] {
            multisampledColorTexture.getImageView(),
            getResolvedColorImageView(frameBufferIndex),
        });
    }

    @Override
    public boolean requiresGeometryRenderContext() {
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
        vkCmdBindPipeline(commandBuffer, VK_PIPELINE_BIND_POINT_GRAPHICS, renderPipeline.getGraphicsPipeline());
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
