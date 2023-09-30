package com.destrostudios.icetea.core.render.shadow;

import com.destrostudios.icetea.core.buffer.PushConstantsDataBuffer;
import com.destrostudios.icetea.core.camera.SceneCamera;
import com.destrostudios.icetea.core.buffer.UniformDataBuffer;
import com.destrostudios.icetea.core.camera.projections.PerspectiveProjection;
import com.destrostudios.icetea.core.light.DirectionalLight;
import com.destrostudios.icetea.core.render.RenderAction;
import com.destrostudios.icetea.core.resource.descriptor.ShadowMapTextureDescriptor;
import com.destrostudios.icetea.core.render.RenderJob;
import com.destrostudios.icetea.core.resource.descriptor.UniformDescriptor;
import com.destrostudios.icetea.core.scene.Geometry;
import com.destrostudios.icetea.core.light.Light;
import com.destrostudios.icetea.core.texture.Texture;
import lombok.Getter;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.*;

import java.nio.LongBuffer;
import java.util.List;
import java.util.function.Consumer;

import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.vulkan.VK10.*;

public class ShadowMapRenderJob extends RenderJob<ShadowMapGeometryRenderContext> {

    public ShadowMapRenderJob(Light light, ShadowConfig shadowConfig) {
        this.light = light;
        this.shadowConfig = shadowConfig;
        shadowMapTexture = new Texture();
        shadowMapTexture.setDescriptor("default", new ShadowMapTextureDescriptor());
        shadowInfoUniformBuffer = new UniformDataBuffer();
        shadowInfoUniformBuffer.setDescriptor("default", new UniformDescriptor(VK_SHADER_STAGE_VERTEX_BIT | VK_SHADER_STAGE_FRAGMENT_BIT));
        pushConstants = new PushConstantsDataBuffer();
    }
    @Getter
    private Light light;
    @Getter
    private ShadowConfig shadowConfig;
    @Getter
    private Texture shadowMapTexture;
    private long[] shadowMapCascadeImageViews;
    @Getter
    private UniformDataBuffer shadowInfoUniformBuffer;
    @Getter
    private PushConstantsDataBuffer pushConstants;

    @Override
    protected void initNative() {
        super.initNative();
        initRenderPass();
        initShadowMapTexture();
        initFrameBuffer();
        initPushConstants();
    }

    @Override
    protected VkExtent2D calculateExtent() {
        return VkExtent2D.create().set(shadowConfig.getShadowMapSize(), shadowConfig.getShadowMapSize());
    }

    private void initRenderPass() {
        try (MemoryStack stack = stackPush()) {
            VkAttachmentDescription.Buffer attachments = VkAttachmentDescription.callocStack(1, stack);
            VkAttachmentReference.Buffer attachmentRefs = VkAttachmentReference.callocStack(1, stack);

            // Depth attachment (shadow map)
            VkAttachmentDescription depthAttachment = attachments.get(0);
            depthAttachment.format(shadowConfig.getShadowMapFormat());
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
            int result = vkCreateRenderPass(application.getLogicalDevice(), renderPassCreateInfo, null, pRenderPass);
            if (result != VK_SUCCESS) {
                throw new RuntimeException("Failed to create render pass (result = " + result + ")");
            }
            renderPass = pRenderPass.get(0);
        }
    }

    private void initShadowMapTexture() {
        try (MemoryStack stack = stackPush()) {
            LongBuffer pImage = stack.mallocLong(1);
            LongBuffer pImageMemory = stack.mallocLong(1);
            application.getImageManager().createImage(
                shadowConfig.getShadowMapSize(),
                shadowConfig.getShadowMapSize(),
                1,
                VK_SAMPLE_COUNT_1_BIT,
                shadowConfig.getShadowMapFormat(),
                VK_IMAGE_USAGE_DEPTH_STENCIL_ATTACHMENT_BIT | VK_IMAGE_USAGE_SAMPLED_BIT,
                VK_MEMORY_PROPERTY_DEVICE_LOCAL_BIT,
                shadowConfig.getCascadesCount(),
                pImage,
                pImageMemory
            );
            long image = pImage.get(0);
            long imageMemory = pImageMemory.get(0);

            // Image view with all cascade layers, used to read the values inside the fragment shader (which calculates cascadeIndex and does lookup)
            long imageView = application.getImageManager().createImageView(
                image,
                shadowConfig.getShadowMapFormat(),
                VK_IMAGE_ASPECT_DEPTH_BIT,
                1,
                VK_IMAGE_VIEW_TYPE_2D_ARRAY,
                shadowConfig.getCascadesCount(),
                0
            );

            // Image views for each cascade layer, used as framebuffer attachment to render to that specific layer
            shadowMapCascadeImageViews = new long[shadowConfig.getCascadesCount()];
            for (int i = 0; i < shadowMapCascadeImageViews.length; i++) {
                shadowMapCascadeImageViews[i] = application.getImageManager().createImageView(
                    image,
                    shadowConfig.getShadowMapFormat(),
                    VK_IMAGE_ASPECT_DEPTH_BIT,
                    1,
                    VK_IMAGE_VIEW_TYPE_2D_ARRAY,
                    1,
                    i
                );
            }

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
            int result = vkCreateSampler(application.getLogicalDevice(), samplerCreateInfo, null, pImageSampler);
            if (result != VK_SUCCESS) {
                throw new RuntimeException("Failed to create image sampler (result = " + result + ")");
            }
            long imageSampler = pImageSampler.get(0);

            // Will later be true because of the specified attachment transition after renderpass
            int finalLayout = VK_IMAGE_LAYOUT_DEPTH_STENCIL_READ_ONLY_OPTIMAL;
            shadowMapTexture.set(image, imageMemory, imageView, finalLayout, imageSampler);
        }
    }

    private void initFrameBuffer() {
        initFrameBuffers(frameBufferIndex -> new long[] {
            shadowMapCascadeImageViews[frameBufferIndex]
        }, shadowConfig.getCascadesCount());
    }

    private void initPushConstants() {
        pushConstants.getData().setInt("cascadeIndex", 0);
    }

    @Override
    public List<Long> getFrameBuffersToRender(int imageIndex) {
        return frameBuffers;
    }

    @Override
    public void updateNative() {
        super.updateNative();
        shadowMapTexture.updateNative(application);
        updateShadowInfoUniformBuffer();
        shadowInfoUniformBuffer.updateNative(application);
        pushConstants.updateNative(application);
    }

    private void updateShadowInfoUniformBuffer() {
        // Calculate frustum split depths and matrices for the shadow map cascades (Based on https://johanmedestrom.wordpress.com/2016/03/18/opengl-cascaded-shadow-maps)
        float[] splitDepths = new float[shadowConfig.getCascadesCount()];
        Matrix4f[] viewProjectionMatrices = new Matrix4f[shadowConfig.getCascadesCount()];

        SceneCamera camera = application.getSceneCamera();
        float nearClip = camera.getZNear();
        float farClip = camera.getZFar();
        float clipRange = farClip - nearClip;

        float[] cascadeSplits = shadowConfig.getCascadeSplits();
        if (cascadeSplits != null) {
            if (cascadeSplits.length != shadowConfig.getCascadesCount()) {
                throw new IllegalArgumentException("Cascade splits length has to be equal to cascades count.");
            }
        } else {
            cascadeSplits = new float[shadowConfig.getCascadesCount()];
            if (camera.getProjection() instanceof PerspectiveProjection) {
                // Calculate split depths based on view camera frustum (Based on https://developer.nvidia.com/gpugems/GPUGems3/gpugems3_ch10.html)
                // This calculation assumes both zNear and zFar are positive (which should be true for perspective), as otherwise "ratio" might be negative, resulting in a lot of NaNs
                float minZ = nearClip;
                float maxZ = nearClip + clipRange;
                float range = maxZ - minZ;
                float ratio = maxZ / minZ;
                for (int i = 0; i < shadowConfig.getCascadesCount(); i++) {
                    float p = (i + 1) / ((float) shadowConfig.getCascadesCount());
                    float log = minZ * (float) Math.pow(ratio, p);
                    float uniform = minZ + (range * p);
                    float d = (shadowConfig.getCascadeSplitLambda() * (log - uniform)) + uniform;
                    cascadeSplits[i] = (d - nearClip) / clipRange;
                }
            } else {
                // Actually ideal for orthographic projection
                for (int i = 0; i < shadowConfig.getCascadesCount(); i++) {
                    cascadeSplits[i] = ((i + 1) / ((float) shadowConfig.getCascadesCount()));
                }
            }
        }

        // Calculate orthographic projection matrix for each cascade
        float lastSplitDist = 0;
        for (int i = 0; i < shadowConfig.getCascadesCount(); i++) {
            float splitDist = cascadeSplits[i];

            Vector3f[] frustumCorners = new Vector3f[] {
                new Vector3f(-1,  1, 0),
                new Vector3f( 1,  1, 0),
                new Vector3f( 1, -1, 0),
                new Vector3f(-1, -1, 0),
                new Vector3f(-1,  1,  1),
                new Vector3f( 1,  1,  1),
                new Vector3f( 1, -1,  1),
                new Vector3f(-1, -1,  1),
			};

            // Project frustum corners into world space
            Matrix4f invCam = camera.getProjectionViewMatrix().invert(new Matrix4f());
            for (int r = 0; r < 8; r++) {
                Vector4f invCorner = new Vector4f(frustumCorners[r].x(), frustumCorners[r].y(), frustumCorners[r].z(), 1).mul(invCam);
                frustumCorners[r] = new Vector3f(invCorner.x(), invCorner.y(), invCorner.z()).div(invCorner.w());
            }

            for (int r = 0; r < 4; r++) {
                Vector3f dist = frustumCorners[r + 4].sub(frustumCorners[r], new Vector3f());
                frustumCorners[r + 4] = frustumCorners[r].add(dist.mul(splitDist, new Vector3f()), new Vector3f());
                frustumCorners[r] = frustumCorners[r].add(dist.mul(lastSplitDist, new Vector3f()), new Vector3f());
            }

            // Get frustum center
            Vector3f frustumCenter = new Vector3f();
            for (int r = 0; r < 8; r++) {
                frustumCenter.add(frustumCorners[r]);
            }
            frustumCenter.div(8);

            float radius = 0;
            for (int r = 0; r < 8; r++) {
                float distance = frustumCorners[r].sub(frustumCenter, new Vector3f()).length();
                radius = Math.max(radius, distance);
            }
            radius = (float) (Math.ceil(radius * 16) / 16);

            Vector3f maxExtents = new Vector3f(radius);
            Vector3f minExtents = maxExtents.negate(new Vector3f());

            Matrix4f lightViewMatrix = new Matrix4f();
            Matrix4f lightOrthoMatrix = new Matrix4f();

            if (light instanceof DirectionalLight directionalLight) {
                lightOrthoMatrix.ortho(
                    minExtents.x(),
                    maxExtents.x(),
                    minExtents.y(),
                    maxExtents.y(),
                    0,
                    maxExtents.z() - minExtents.z(),
                    true
                );
                lightOrthoMatrix.m11(lightOrthoMatrix.m11() * -1);

                lightViewMatrix.lookAt(
                    frustumCenter.sub(directionalLight.getDirection().mul(-1 * minExtents.z(), new Vector3f()), new Vector3f()),
                    frustumCenter,
                    // TODO: This doesn't work if the light direction is on the up axis
                    new Vector3f(0, 0, 1)
                );
            } else {
                // TODO: Support all lights
                throw new IllegalArgumentException("Currently only directional lights support shadows.");
            }

            splitDepths[i] = -1 * (nearClip + (splitDist * clipRange));
            viewProjectionMatrices[i] = lightOrthoMatrix.mul(lightViewMatrix, new Matrix4f());

            lastSplitDist = cascadeSplits[i];
        }
        shadowInfoUniformBuffer.getData().setFloatArray("splitDepths", splitDepths);
        shadowInfoUniformBuffer.getData().setMatrix4fArray("viewProjectionMatrices", viewProjectionMatrices);
        // FIXME: These two have to be defined last or somehow the memory alignment is messed up
        shadowInfoUniformBuffer.getData().setBoolean("cascadeDebugColors", shadowConfig.isCascadeDebugColors());
        shadowInfoUniformBuffer.getData().setFloat("brightness", shadowConfig.getBrightness());
    }

    @Override
    protected boolean isRendering(Geometry geometry) {
        return geometry.isCastingShadows();
    }

    @Override
    protected ShadowMapGeometryRenderContext createGeometryRenderContext(Geometry geometry) {
        return new ShadowMapGeometryRenderContext(geometry, this);
    }

    @Override
    public VkClearValue.Buffer getClearValues(MemoryStack stack) {
        VkClearValue.Buffer clearValues = VkClearValue.callocStack(1, stack);
        clearValues.get(0).depthStencil().set(1, 0);
        return clearValues;
    }

    @Override
    public void render(Consumer<RenderAction> actions) {
        actions.accept(rt -> pushConstants.getData().setInt("cascadeIndex", rt.getFrameBufferIndex()));
        pushConstants.updateNative(application);

        application.getRootNode().forEachGeometry(geometry -> {
            ShadowMapGeometryRenderContext renderContext = getRenderContext(geometry);
            if (renderContext != null) {
                actions.accept(rt -> vkCmdPushConstants(rt.getCommandBuffer(), renderContext.getRenderPipeline().getPipelineLayout(), VK_SHADER_STAGE_VERTEX_BIT, 0, pushConstants.getBuffer().getByteBuffer()));
                geometry.getRenderer().render(geometry, renderContext, actions);
            }
        });
    }

    @Override
    protected void cleanupNativeInternal() {
        pushConstants.cleanupNative();
        shadowInfoUniformBuffer.cleanupNative();
        for (long imageView : shadowMapCascadeImageViews) {
            vkDestroyImageView(application.getLogicalDevice(), imageView, null);
        }
        shadowMapTexture.cleanupNative();
        super.cleanupNativeInternal();
    }
}
