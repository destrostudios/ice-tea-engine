package com.destrostudios.icetea.core.render.shadow;

import com.destrostudios.icetea.core.*;
import com.destrostudios.icetea.core.data.UniformData;
import com.destrostudios.icetea.core.light.DirectionalLight;
import com.destrostudios.icetea.core.light.SpotLight;
import com.destrostudios.icetea.core.render.GeometryRenderContext;
import com.destrostudios.icetea.core.render.RenderJob;
import com.destrostudios.icetea.core.render.RenderPipeline;
import com.destrostudios.icetea.core.scene.Geometry;
import com.destrostudios.icetea.core.light.Light;
import com.destrostudios.icetea.core.texture.Texture;
import com.destrostudios.icetea.core.util.MathUtil;
import lombok.Getter;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.*;

import java.nio.LongBuffer;

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
    private UniformData lightTransformUniformData;

    @Override
    public void init(Application application) {
        super.init(application);
        initRenderPass();
        initShadowMapTexture();
        initFrameBuffer();
        initLightTransform();
    }

    @Override
    public void updateUniformBuffers(int currentImage) {
        super.updateUniformBuffers(currentImage);
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
        lightTransformUniformData.updateBufferIfNecessary(currentImage);
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

            // Subpass 0: Shadow map rendering
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
        initFrameBuffers(frameBufferIndex -> new long[] {
            shadowMapTexture.getImageView()
        });
    }

    private void initLightTransform() {
        lightTransformUniformData = new UniformData();
        lightTransformUniformData.setApplication(application);
        lightTransformUniformData.setMatrix4f("proj", new Matrix4f());
        lightTransformUniformData.setMatrix4f("view", new Matrix4f());
        lightTransformUniformData.initBuffers(application.getSwapChain().getImages().size());
    }

    @Override
    public boolean isRendering(Geometry geometry) {
        return geometry.hasParent(application.getSceneNode());
    }

    @Override
    public ShadowMapGeometryRenderContext createGeometryRenderContext() {
        return new ShadowMapGeometryRenderContext();
    }

    @Override
    public VkClearValue.Buffer getClearValues(MemoryStack stack) {
        VkClearValue.Buffer clearValues = VkClearValue.callocStack(1, stack);
        clearValues.get(0).depthStencil().set(1, 0);
        return clearValues;
    }

    @Override
    public void render(VkCommandBuffer commandBuffer, int commandBufferIndex, MemoryStack stack) {
        application.getRootNode().forEachGeometry(geometry -> {
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
            shadowMapTexture.cleanup();
            lightTransformUniformData.cleanupBuffer();
        }
        super.cleanup();
    }
}
