package com.destrostudios.icetea.core.render.cubemap;

import com.destrostudios.icetea.core.Pipeline;
import com.destrostudios.icetea.core.buffer.PushConstantsDataBuffer;
import com.destrostudios.icetea.core.mesh.Box;
import com.destrostudios.icetea.core.render.MeshRenderer;
import com.destrostudios.icetea.core.render.RenderJob;
import com.destrostudios.icetea.core.render.RenderRecorder;
import com.destrostudios.icetea.core.render.RenderTask;
import com.destrostudios.icetea.core.resource.ResourceDescriptorSet;
import com.destrostudios.icetea.core.resource.descriptor.CubeMapTextureDescriptor;
import com.destrostudios.icetea.core.shader.Shader;
import com.destrostudios.icetea.core.texture.Texture;
import com.destrostudios.icetea.core.util.ImageUtil;
import lombok.Getter;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.*;

import java.nio.LongBuffer;
import java.util.List;

import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.vulkan.VK10.*;

public abstract class CubeMapRenderJob extends RenderJob<CubeMapRenderPipelineCreator> {

    private static final Matrix4f[] MODEL_VIEW_PROJECTION_MATRICES = new Matrix4f[] {
        createModelViewProjectionMatrix(new Matrix4f().rotate((float) (Math.PI / 2), new Vector3f(0, 1, 0)).rotate((float) Math.PI, new Vector3f(1, 0, 0))),
        createModelViewProjectionMatrix(new Matrix4f().rotate((float) (Math.PI / -2), new Vector3f(0, 1, 0)).rotate((float) Math.PI, new Vector3f(1, 0, 0))),
        createModelViewProjectionMatrix(new Matrix4f().rotate((float) (Math.PI / -2), new Vector3f(1, 0, 0))),
        createModelViewProjectionMatrix(new Matrix4f().rotate((float) (Math.PI / 2), new Vector3f(1, 0, 0))),
        createModelViewProjectionMatrix(new Matrix4f().rotate((float) Math.PI, new Vector3f(1, 0, 0))),
        createModelViewProjectionMatrix(new Matrix4f().rotate((float) Math.PI, new Vector3f(0, 0, 1)))
    };

    private static Matrix4f createModelViewProjectionMatrix(Matrix4f rotationMatrix) {
        return new Matrix4f().perspective((float) (Math.PI / 2), 1, 0.1f, 512).mul(rotationMatrix);
    }

    public CubeMapRenderJob() {
        super("cubeMap");
        autoBeginAndEndRenderPass = false;
        cubeMapConfig = new CubeMapConfig();
        faceTexture = new Texture();
        cubeMapTexture = new Texture();
        cubeMapTexture.setDescriptor("default", new CubeMapTextureDescriptor());
        pushConstants = new PushConstantsDataBuffer();
        cubeMesh = new Box(false, false);
        meshRenderer = new MeshRenderer();
    }
    protected CubeMapConfig cubeMapConfig;
    private Texture faceTexture;
    @Getter
    protected Texture cubeMapTexture;
    @Getter
    protected ResourceDescriptorSet resourceDescriptorSet;
    @Getter
    protected PushConstantsDataBuffer pushConstants;
    private Box cubeMesh;
    private MeshRenderer meshRenderer;
    private Pipeline renderPipeline;
    protected boolean cleanupCubeMapTexture;

    @Override
    protected void initNative() {
        super.initNative();
        try (MemoryStack stack = stackPush()) {
            initTextures(stack);
            initPushConstants();
            initRenderPass(stack);
            initFrameBuffers();
            initResourceDescriptorSet();
            initRenderPipeline();
        }
    }

    @Override
    protected VkExtent2D calculateExtent() {
        return VkExtent2D.create().set(cubeMapConfig.getSize(), cubeMapConfig.getSize());
    }

    private void initTextures(MemoryStack stack) {
        int mipLevels = cubeMapConfig.isGenerateMipMaps() ? ImageUtil.getRecommendedMipLevels(cubeMapConfig.getSize()) : 1;

        initColorTexture(
            faceTexture,
            cubeMapConfig.getFormat(),
            1,
            VK_IMAGE_USAGE_COLOR_ATTACHMENT_BIT | VK_IMAGE_USAGE_TRANSFER_SRC_BIT,
            false,
            stack
        );

        initCubeMapTexture(
            cubeMapTexture,
            cubeMapConfig.getFormat(),
            mipLevels,
            VK_IMAGE_USAGE_TRANSFER_DST_BIT | VK_IMAGE_USAGE_SAMPLED_BIT,
            stack
        );
        cubeMapTexture.setAutomaticallyTransitionedLayout(VK_IMAGE_LAYOUT_SHADER_READ_ONLY_OPTIMAL);

        application.getCommandPool().executeSingleTimeCommands(commandBuffer -> {
            faceTexture.transitionLayout(
                commandBuffer,
                VK_IMAGE_LAYOUT_COLOR_ATTACHMENT_OPTIMAL,
                VK_PIPELINE_STAGE_TOP_OF_PIPE_BIT,
                0,
                VK_PIPELINE_STAGE_EARLY_FRAGMENT_TESTS_BIT,
                VK_ACCESS_DEPTH_STENCIL_ATTACHMENT_READ_BIT | VK_ACCESS_DEPTH_STENCIL_ATTACHMENT_WRITE_BIT,
                stack
            );
            cubeMapTexture.transitionLayout(
                commandBuffer,
                VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL,
                VK_PIPELINE_STAGE_ALL_COMMANDS_BIT,
                0,
                VK_PIPELINE_STAGE_ALL_COMMANDS_BIT,
                VK_ACCESS_TRANSFER_WRITE_BIT,
                stack
            );
        });
    }

    protected void initPushConstants() {
        pushConstants.getData().setMatrix4f("modelViewProjectionMatrix", new Matrix4f());
    }

    private void initRenderPass(MemoryStack stack) {
        VkAttachmentDescription.Buffer attachments = VkAttachmentDescription.calloc(1, stack);
        VkAttachmentReference.Buffer attachmentRefs = VkAttachmentReference.calloc(1, stack);

        // Color attachment (Face)

        VkAttachmentDescription colorAttachment = attachments.get(0);
        colorAttachment.format(cubeMapConfig.getFormat());
        colorAttachment.samples(VK_SAMPLE_COUNT_1_BIT);
        colorAttachment.loadOp(VK_ATTACHMENT_LOAD_OP_DONT_CARE);
        colorAttachment.storeOp(VK_ATTACHMENT_STORE_OP_STORE);
        colorAttachment.stencilLoadOp(VK_ATTACHMENT_LOAD_OP_DONT_CARE);
        colorAttachment.stencilStoreOp(VK_ATTACHMENT_STORE_OP_DONT_CARE);
        colorAttachment.initialLayout(VK_IMAGE_LAYOUT_UNDEFINED);
        colorAttachment.finalLayout(VK_IMAGE_LAYOUT_COLOR_ATTACHMENT_OPTIMAL);

        VkAttachmentReference colorAttachmentRef = attachmentRefs.get(0);
        colorAttachmentRef.attachment(0);
        colorAttachmentRef.layout(VK_IMAGE_LAYOUT_COLOR_ATTACHMENT_OPTIMAL);

        // Subpass and dependencies

        VkSubpassDescription.Buffer subpass = VkSubpassDescription.calloc(1, stack);
        subpass.pipelineBindPoint(VK_PIPELINE_BIND_POINT_GRAPHICS);
        subpass.colorAttachmentCount(1);
        subpass.pColorAttachments(VkAttachmentReference.calloc(1, stack).put(0, colorAttachmentRef));

        VkSubpassDependency.Buffer dependencies = VkSubpassDependency.calloc(2, stack);

        VkSubpassDependency dependency1 = dependencies.get(0);
        dependency1.srcSubpass(VK_SUBPASS_EXTERNAL);
        dependency1.dstSubpass(0);
        dependency1.srcStageMask(VK_PIPELINE_STAGE_BOTTOM_OF_PIPE_BIT);
        dependency1.srcAccessMask(VK_ACCESS_MEMORY_READ_BIT);
        dependency1.dstStageMask(VK_PIPELINE_STAGE_COLOR_ATTACHMENT_OUTPUT_BIT);
        dependency1.dstAccessMask(VK_ACCESS_COLOR_ATTACHMENT_READ_BIT | VK_ACCESS_COLOR_ATTACHMENT_WRITE_BIT);
        dependency1.dependencyFlags(VK_DEPENDENCY_BY_REGION_BIT);

        VkSubpassDependency dependency2 = dependencies.get(1);
        dependency2.srcSubpass(0);
        dependency2.dstSubpass(VK_SUBPASS_EXTERNAL);
        dependency2.srcStageMask(VK_PIPELINE_STAGE_COLOR_ATTACHMENT_OUTPUT_BIT);
        dependency2.srcAccessMask(VK_ACCESS_COLOR_ATTACHMENT_READ_BIT | VK_ACCESS_COLOR_ATTACHMENT_WRITE_BIT);
        dependency2.dstStageMask(VK_PIPELINE_STAGE_BOTTOM_OF_PIPE_BIT);
        dependency2.dstAccessMask(VK_ACCESS_MEMORY_READ_BIT);
        dependency2.dependencyFlags(VK_DEPENDENCY_BY_REGION_BIT);

        VkRenderPassCreateInfo renderPassCreateInfo = VkRenderPassCreateInfo.calloc(stack);
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

    private void initFrameBuffers() {
        initFrameBuffers(_ -> new long[] {
            faceTexture.getImageView(),
        });
    }

    protected void initResourceDescriptorSet() {
        resourceDescriptorSet = new ResourceDescriptorSet();
    }

    private void initRenderPipeline() {
        renderPipelineCreator = new CubeMapRenderPipelineCreator(application, this);

        CubeMapRenderPipelineState state = new CubeMapRenderPipelineState("renderCubeMap");
        cubeMesh.updateNative(application);
        renderPipelineCreator.fillMeshState(state, cubeMesh);
        state.setFragmentShader(getFragmentShader());
        state.setDescriptorSetShaderDeclaration(resourceDescriptorSet.getShaderDeclaration());
        renderPipeline = renderPipelineCreator.getOrCreatePipeline(state, resourceDescriptorSet);
    }

    public abstract Shader getFragmentShader();

    @Override
    public List<RenderTask> render(MemoryStack stack) {
        return List.of(recorder -> {
            recorder.bindPipeline(renderPipeline);
            recorder.bindDescriptorSets(resourceDescriptorSet, stack);

            VkViewport.Buffer viewport = VkViewport.calloc(1, stack);
            viewport.minDepth(0);
            viewport.maxDepth(1);

            for (int mipLevel = 0; mipLevel < cubeMapTexture.getMipLevels(); mipLevel++) {
                int mipLevelSize = (int) (cubeMapConfig.getSize() * (float) Math.pow(0.5f, mipLevel));
                viewport.width(mipLevelSize);
                viewport.height(mipLevelSize);
                vkCmdSetViewport(recorder.getCommandBuffer(), 0, viewport);
                for (int face = 0; face < 6; face++) {
                    recorder.beginRenderPass(this, stack);

                    updatePushConstants(mipLevel, face);
                    pushConstants.updateNative(application);
                    vkCmdPushConstants(recorder.getCommandBuffer(), renderPipeline.getPipelineLayout(), VK_SHADER_STAGE_VERTEX_BIT | VK_SHADER_STAGE_FRAGMENT_BIT, 0, pushConstants.getBuffer().getByteBuffer());

                    meshRenderer.drawMesh(recorder, cubeMesh, stack);

                    recorder.endRenderPass();

                    copyFaceToCubeTexture(recorder, mipLevel, face, mipLevelSize, stack);
                }
            }
        });
    }

    protected void updatePushConstants(int mipLevel, int face) {
        pushConstants.getData().setMatrix4f("modelViewProjectionMatrix", MODEL_VIEW_PROJECTION_MATRICES[face]);
    }

    private void copyFaceToCubeTexture(RenderRecorder recorder, int mipLevel, int face, int mipLevelSize, MemoryStack stack) {
        faceTexture.transitionLayout(
            recorder.getCommandBuffer(),
            VK_IMAGE_LAYOUT_TRANSFER_SRC_OPTIMAL,
            VK_PIPELINE_STAGE_ALL_COMMANDS_BIT,
            VK_ACCESS_COLOR_ATTACHMENT_WRITE_BIT,
            VK_PIPELINE_STAGE_ALL_COMMANDS_BIT,
            VK_ACCESS_TRANSFER_READ_BIT,
            stack
        );

        cubeMapTexture.copyFromTexture(
            recorder.getCommandBuffer(),
            faceTexture,
            face,
            mipLevel,
            mipLevelSize,
            mipLevelSize,
            stack
        );

        faceTexture.transitionLayout(
            recorder.getCommandBuffer(),
            VK_IMAGE_LAYOUT_COLOR_ATTACHMENT_OPTIMAL,
            VK_PIPELINE_STAGE_ALL_COMMANDS_BIT,
            VK_ACCESS_TRANSFER_READ_BIT,
            VK_PIPELINE_STAGE_ALL_COMMANDS_BIT,
            VK_ACCESS_COLOR_ATTACHMENT_WRITE_BIT,
            stack
        );
    }

    @Override
    public void postRender(RenderRecorder recorder, MemoryStack stack) {
        super.postRender(recorder, stack);
        application.getCommandPool().executeSingleTimeCommands(commandBuffer -> {
            cubeMapTexture.transitionLayout(
                commandBuffer,
                VK_IMAGE_LAYOUT_SHADER_READ_ONLY_OPTIMAL,
                VK_PIPELINE_STAGE_ALL_COMMANDS_BIT,
                VK_ACCESS_TRANSFER_WRITE_BIT,
                VK_PIPELINE_STAGE_ALL_COMMANDS_BIT,
                VK_ACCESS_SHADER_READ_BIT,
                stack
            );
        });
    }

    @Override
    public void updateNative() {
        super.updateNative();
        faceTexture.updateNative(application);
        cubeMapTexture.updateNative(application);
        cubeMesh.updateNative(application);
        renderPipeline.updateNative(application);
    }

    @Override
    protected void cleanupNativeInternal() {
        // Don't cleanup (the potentially shared) renderPipeline to keep it in the PipelineManager cache (which owns and controls its lifetime)
        cubeMesh.cleanupNative();
        if (cleanupCubeMapTexture) {
            cubeMapTexture.cleanupNative();
        }
        faceTexture.cleanupNative();
        super.cleanupNativeInternal();
    }
}
