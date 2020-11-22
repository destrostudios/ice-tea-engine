package com.destrostudios.icetea.core;

import com.destrostudios.icetea.core.lights.DirectionalLight;
import com.destrostudios.icetea.core.lights.SpotLight;
import com.destrostudios.icetea.core.materials.descriptors.GeometryTransformDescriptorLayout;
import com.destrostudios.icetea.core.materials.descriptors.ShadowMapLightTransformDescriptorLayout;
import lombok.Getter;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.*;

import java.nio.LongBuffer;
import java.util.ArrayList;

import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.vulkan.VK10.*;

public class ShadowMapRenderJob extends RenderJob<ShadowMapGeometryRenderContext> {

    public ShadowMapRenderJob(Light light, int shadowMapSize) {
        this.light = light;
        shadowMapWidth = shadowMapSize;
        shadowMapHeight = shadowMapSize;
    }
    @Getter
    private Light light;
    @Getter
    private int shadowMapWidth;
    @Getter
    private int shadowMapHeight;
    @Getter
    private Texture shadowMapTexture;
    @Getter
    private MaterialDescriptorSetLayout materialDescriptorSetLayout;
    @Getter
    private UniformData lightTransformUniformData;
    private ShadowMapRenderPipeline renderPipeline;

    @Override
    public void init(Application application) {
        super.init(application);
        initRenderPass();
        initShadowMapTexture();
        initFrameBuffer();
        initDescriptorSetLayout();
        initLightTransform();
    }

    @Override
    public void updateUniformBuffers(int currentImage, MemoryStack stack) {
        super.updateUniformBuffers(currentImage, stack);
        Matrix4f projectionMatrix = new Matrix4f();
        Matrix4f viewMatrix = new Matrix4f();
        if (light instanceof DirectionalLight) {
            DirectionalLight directionalLight = (DirectionalLight) light;
            projectionMatrix.ortho(-6, 6, -2, 2, -6, 6, true);
            projectionMatrix.m11(projectionMatrix.m11() * -1);

            viewMatrix.lookAt(directionalLight.getDirection().negate(new Vector3f()), new Vector3f(0, 0, 0), new Vector3f(0, 0, 1));
        } else if (light instanceof SpotLight) {
            SpotLight spotLight = (SpotLight) light;
            projectionMatrix.perspective((float) Math.toDegrees(45), ((float) shadowMapWidth) / shadowMapHeight, 0.1f, 100, true);
            projectionMatrix.m11(projectionMatrix.m11() * -1);

            MathUtil.setViewMatrix(viewMatrix, spotLight.getTranslation(), spotLight.getRotation());
        }
        lightTransformUniformData.setMatrix4f("proj", projectionMatrix);
        lightTransformUniformData.setMatrix4f("view", viewMatrix);
        lightTransformUniformData.updateBufferIfNecessary(currentImage, stack);
    }

    @Override
    protected VkExtent2D calculateExtent() {
        return VkExtent2D.create().set(shadowMapWidth, shadowMapHeight);
    }

    private void initRenderPass() {
        try (MemoryStack stack = stackPush()) {
            VkAttachmentDescription.Buffer attachments = VkAttachmentDescription.callocStack(1, stack);
            VkAttachmentReference.Buffer attachmentRefs = VkAttachmentReference.callocStack(1, stack);

            // Depth attachment (shadow map)
            VkAttachmentDescription depthAttachment = attachments.get(0);
            depthAttachment.format(VK_FORMAT_D16_UNORM);
            depthAttachment.samples(VK_SAMPLE_COUNT_1_BIT);
            depthAttachment.loadOp(VK_ATTACHMENT_LOAD_OP_CLEAR);
            depthAttachment.storeOp(VK_ATTACHMENT_STORE_OP_STORE);
            depthAttachment.stencilLoadOp(VK_ATTACHMENT_LOAD_OP_DONT_CARE);
            depthAttachment.stencilStoreOp(VK_ATTACHMENT_STORE_OP_DONT_CARE);
            depthAttachment.initialLayout(VK_IMAGE_LAYOUT_UNDEFINED);
            depthAttachment.finalLayout(VK_IMAGE_LAYOUT_DEPTH_STENCIL_READ_ONLY_OPTIMAL);

            // Attachment references from subpasses
            VkAttachmentReference depthAttachmentRef = attachmentRefs.get(0);
            depthAttachmentRef.attachment(0);
            depthAttachmentRef.layout(VK_IMAGE_LAYOUT_DEPTH_STENCIL_ATTACHMENT_OPTIMAL);

            // Subpass 0: shadow map rendering
            VkSubpassDescription.Buffer subpass = VkSubpassDescription.callocStack(1, stack);
            subpass.pipelineBindPoint(VK_PIPELINE_BIND_POINT_GRAPHICS);
            subpass.pDepthStencilAttachment(depthAttachmentRef);

            VkSubpassDependency.Buffer dependencies = VkSubpassDependency.callocStack(2, stack);

            VkSubpassDependency dependency1 = dependencies.get(0);
            dependency1.srcSubpass(VK_SUBPASS_EXTERNAL);
            dependency1.dstSubpass(0);
            dependency1.srcStageMask(VK_PIPELINE_STAGE_FRAGMENT_SHADER_BIT);
            dependency1.srcAccessMask(VK_ACCESS_SHADER_READ_BIT);
            dependency1.dstStageMask(VK_PIPELINE_STAGE_EARLY_FRAGMENT_TESTS_BIT);
            dependency1.dstAccessMask(VK_ACCESS_DEPTH_STENCIL_ATTACHMENT_WRITE_BIT);
            dependency1.dependencyFlags(VK_DEPENDENCY_BY_REGION_BIT);

            VkSubpassDependency dependency2 = dependencies.get(1);
            dependency2.srcSubpass(0);
            dependency2.dstSubpass(VK_SUBPASS_EXTERNAL);
            dependency2.srcStageMask(VK_PIPELINE_STAGE_LATE_FRAGMENT_TESTS_BIT);
            dependency2.srcAccessMask(VK_ACCESS_DEPTH_STENCIL_ATTACHMENT_WRITE_BIT);
            dependency2.dstStageMask(VK_PIPELINE_STAGE_FRAGMENT_SHADER_BIT);
            dependency2.dstAccessMask(VK_ACCESS_SHADER_READ_BIT);
            dependency2.dependencyFlags(VK_DEPENDENCY_BY_REGION_BIT);

            VkRenderPassCreateInfo renderPassCreateInfo = VkRenderPassCreateInfo.callocStack(stack);
            renderPassCreateInfo.sType(VK_STRUCTURE_TYPE_RENDER_PASS_CREATE_INFO);
            renderPassCreateInfo.pAttachments(attachments);
            renderPassCreateInfo.pSubpasses(subpass);
            renderPassCreateInfo.pDependencies(dependencies);

            LongBuffer pRenderPass = stack.mallocLong(1);
            if (vkCreateRenderPass(application.getLogicalDevice(), renderPassCreateInfo, null, pRenderPass) != VK_SUCCESS) {
                throw new RuntimeException("Failed to create render pass");
            }
            renderPass = pRenderPass.get(0);
        }
    }

    private void initShadowMapTexture() {
        try (MemoryStack stack = stackPush()) {
            LongBuffer pImage = stack.mallocLong(1);
            LongBuffer pImageMemory = stack.mallocLong(1);
            application.getImageManager().createImage(
                shadowMapWidth,
                shadowMapHeight,
                1,
                VK_SAMPLE_COUNT_1_BIT,
                VK_FORMAT_D16_UNORM,
                VK_IMAGE_TILING_OPTIMAL,
                VK_IMAGE_USAGE_DEPTH_STENCIL_ATTACHMENT_BIT | VK_IMAGE_USAGE_SAMPLED_BIT,
                VK_MEMORY_PROPERTY_DEVICE_LOCAL_BIT,
                pImage,
                pImageMemory
            );
            long image = pImage.get(0);
            long imageMemory = pImageMemory.get(0);

            long imageView = application.getImageManager().createImageView(
                image,
                VK_FORMAT_D16_UNORM,
                VK_IMAGE_ASPECT_DEPTH_BIT,
                1
            );

            VkSamplerCreateInfo samplerCreateInfo = VkSamplerCreateInfo.callocStack(stack);
            samplerCreateInfo.sType(VK_STRUCTURE_TYPE_SAMPLER_CREATE_INFO);
            int filter = VK_FILTER_LINEAR;
            samplerCreateInfo.magFilter(filter);
            samplerCreateInfo.minFilter(filter);
            samplerCreateInfo.addressModeU(VK_SAMPLER_ADDRESS_MODE_CLAMP_TO_EDGE);
            samplerCreateInfo.addressModeV(VK_SAMPLER_ADDRESS_MODE_CLAMP_TO_EDGE);
            samplerCreateInfo.addressModeW(VK_SAMPLER_ADDRESS_MODE_CLAMP_TO_EDGE);
            samplerCreateInfo.anisotropyEnable(true);
            samplerCreateInfo.maxAnisotropy(1.0f);
            samplerCreateInfo.borderColor(VK_BORDER_COLOR_FLOAT_OPAQUE_WHITE);
            samplerCreateInfo.mipmapMode(VK_SAMPLER_MIPMAP_MODE_LINEAR);
            samplerCreateInfo.minLod(0);
            samplerCreateInfo.maxLod(1);

            LongBuffer pImageSampler = stack.mallocLong(1);
            if (vkCreateSampler(application.getLogicalDevice(), samplerCreateInfo, null, pImageSampler) != VK_SUCCESS) {
                throw new RuntimeException("Failed to create image sampler");
            }
            long imageSampler = pImageSampler.get(0);

            shadowMapTexture = new Texture(image, imageMemory, imageView, imageSampler);
            shadowMapTexture.init(application);
        }
    }

    private void initFrameBuffer() {
        frameBuffers = new ArrayList<>(1);
        try (MemoryStack stack = stackPush()) {
            LongBuffer attachments = stack.longs(shadowMapTexture.getImageView());
            LongBuffer pFrameBuffer = stack.mallocLong(1);

            VkFramebufferCreateInfo framebufferCreateInfo = VkFramebufferCreateInfo.callocStack(stack);
            framebufferCreateInfo.sType(VK_STRUCTURE_TYPE_FRAMEBUFFER_CREATE_INFO);
            framebufferCreateInfo.renderPass(renderPass);
            framebufferCreateInfo.width(shadowMapWidth);
            framebufferCreateInfo.height(shadowMapHeight);
            framebufferCreateInfo.layers(1);
            framebufferCreateInfo.pAttachments(attachments);

            if (vkCreateFramebuffer(application.getLogicalDevice(), framebufferCreateInfo, null, pFrameBuffer) != VK_SUCCESS) {
                throw new RuntimeException("Failed to create framebuffer");
            }
            frameBuffers.add(pFrameBuffer.get(0));
        }
    }

    private void initDescriptorSetLayout() {
        materialDescriptorSetLayout = new MaterialDescriptorSetLayout(application);
        materialDescriptorSetLayout.addDescriptorLayout(new ShadowMapLightTransformDescriptorLayout());
        materialDescriptorSetLayout.addDescriptorLayout(new GeometryTransformDescriptorLayout());
        materialDescriptorSetLayout.initDescriptorSetLayout();
    }

    private void initLightTransform() {
        lightTransformUniformData = new UniformData();
        lightTransformUniformData.setApplication(application);
        lightTransformUniformData.setMatrix4f("proj", new Matrix4f());
        lightTransformUniformData.setMatrix4f("view", new Matrix4f());
        lightTransformUniformData.initBuffer();
    }

    @Override
    public ShadowMapGeometryRenderContext createGeometryRenderContext() {
        return new ShadowMapGeometryRenderContext();
    }

    public ShadowMapRenderPipeline getOrCreateRenderPipeline(MaterialDescriptorSet referenceMaterialDescriptorSet) {
        if (renderPipeline == null) {
            renderPipeline = new ShadowMapRenderPipeline(application, this, referenceMaterialDescriptorSet);
            renderPipeline.init();
        }
        return renderPipeline;
    }

    @Override
    public VkClearValue.Buffer getClearValues(MemoryStack stack) {
        VkClearValue.Buffer clearValues = VkClearValue.callocStack(1, stack);
        clearValues.get(0).depthStencil().set(1, 0);
        return clearValues;
    }

    @Override
    public long getFramebuffer(int commandBufferIndex) {
        return frameBuffers.get(0);
    }

    @Override
    public void render(VkCommandBuffer commandBuffer, int commandBufferIndex, MemoryStack stack) {
        application.getRootNode().forEachGeometry(geometry -> {
            GeometryRenderContext<?> geometryRenderContext = geometry.getRenderContext(this);
            RenderPipeline<?> renderPipeline = geometryRenderContext.getRenderPipeline();
            vkCmdBindPipeline(commandBuffer, VK_PIPELINE_BIND_POINT_GRAPHICS, renderPipeline.getGraphicsPipeline());
            LongBuffer vertexBuffers = stack.longs(geometry.getMesh().getVertexBuffer());
            LongBuffer offsets = stack.longs(0);
            vkCmdBindVertexBuffers(commandBuffer, 0, vertexBuffers, offsets);
            vkCmdBindIndexBuffer(commandBuffer, geometry.getMesh().getIndexBuffer(), 0, VK_INDEX_TYPE_UINT32);
            vkCmdBindDescriptorSets(commandBuffer, VK_PIPELINE_BIND_POINT_GRAPHICS, renderPipeline.getPipelineLayout(), 0, stack.longs(geometryRenderContext.getDescriptorSet(commandBufferIndex)), null);
            vkCmdDrawIndexed(commandBuffer, geometry.getMesh().getIndices().length, 1, 0, 0, 0);
        });
    }

    @Override
    public void cleanup() {
        if (isInitialized()) {
            shadowMapTexture.cleanup();
            materialDescriptorSetLayout.cleanupDescriptorSetLayout();
        }
        super.cleanup();
    }
}
