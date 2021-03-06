package com.destrostudios.icetea.core.render.scene;

import com.destrostudios.icetea.core.*;
import com.destrostudios.icetea.core.material.Material;
import com.destrostudios.icetea.core.material.descriptor.MaterialDescriptorSet;
import com.destrostudios.icetea.core.render.RenderPipeline;
import com.destrostudios.icetea.core.scene.Geometry;
import com.destrostudios.icetea.core.mesh.Mesh;
import com.destrostudios.icetea.core.shader.ShaderType;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.*;

import java.nio.LongBuffer;
import java.util.LinkedList;

import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.vulkan.VK10.*;
import static org.lwjgl.vulkan.VK10.vkDestroyShaderModule;

public class SceneRenderPipeline extends RenderPipeline<SceneRenderJob> {

    public SceneRenderPipeline(Application application, SceneRenderJob renderJob, Geometry geometry, SceneGeometryRenderContext sceneGeometryRenderContext) {
        super(application, renderJob);
        this.geometry = geometry;
        this.sceneGeometryRenderContext = sceneGeometryRenderContext;
    }
    private Geometry geometry;
    private SceneGeometryRenderContext sceneGeometryRenderContext;

    @Override
    public void init() {
        try (MemoryStack stack = stackPush()) {
            Mesh mesh = geometry.getMesh();
            Material material = geometry.getMaterial();

            MaterialDescriptorSet materialDescriptorSet = sceneGeometryRenderContext.getMaterialDescriptorSet();
            String materialDescriptorSetShaderDeclaration = materialDescriptorSet.getShaderDeclaration();

            int shaderStagesCount = 2;
            if (material.getTesselationControlShader() != null) {
                shaderStagesCount++;
            }
            if (material.getTesselationEvaluationShader() != null) {
                shaderStagesCount++;
            }
            if (material.getGeometryShader() != null) {
                shaderStagesCount++;
            }

            VkPipelineShaderStageCreateInfo.Buffer shaderStages = VkPipelineShaderStageCreateInfo.callocStack(shaderStagesCount, stack);

            int shaderStageIndex = 0;
            LinkedList<Long> shaderModules = new LinkedList<>();

            long vertShaderModule = createShaderModule_Vertex(material.getVertexShader(), materialDescriptorSetShaderDeclaration, mesh);
            createShaderStage(shaderStages, shaderStageIndex, VK_SHADER_STAGE_VERTEX_BIT, vertShaderModule, stack);
            shaderModules.add(vertShaderModule);
            shaderStageIndex++;

            if (material.getTesselationControlShader() != null) {
                long tesselationControlShaderModule = createShaderModule(material.getTesselationControlShader(), ShaderType.TESSELATION_CONTROL_SHADER, materialDescriptorSetShaderDeclaration);
                createShaderStage(shaderStages, shaderStageIndex, VK_SHADER_STAGE_TESSELLATION_CONTROL_BIT, tesselationControlShaderModule, stack);
                shaderModules.add(tesselationControlShaderModule);
                shaderStageIndex++;
            }

            if (material.getTesselationEvaluationShader() != null) {
                long tesselationEvaluationShaderModule = createShaderModule(material.getTesselationEvaluationShader(), ShaderType.TESSELATION_EVALUATION_SHADER, materialDescriptorSetShaderDeclaration);
                createShaderStage(shaderStages, shaderStageIndex, VK_SHADER_STAGE_TESSELLATION_EVALUATION_BIT, tesselationEvaluationShaderModule, stack);
                shaderModules.add(tesselationEvaluationShaderModule);
                shaderStageIndex++;
            }

            if (material.getGeometryShader() != null) {
                long geometryShaderModule = createShaderModule(material.getGeometryShader(), ShaderType.GEOMETRY_SHADER, materialDescriptorSetShaderDeclaration);
                createShaderStage(shaderStages, shaderStageIndex, VK_SHADER_STAGE_GEOMETRY_BIT, geometryShaderModule, stack);
                shaderModules.add(geometryShaderModule);
                shaderStageIndex++;
            }

            long fragShaderModule = createShaderModule(material.getFragmentShader(), ShaderType.FRAGMENT_SHADER, materialDescriptorSetShaderDeclaration);
            createShaderStage(shaderStages, shaderStageIndex, VK_SHADER_STAGE_FRAGMENT_BIT, fragShaderModule, stack);
            shaderModules.add(fragShaderModule);
            shaderStageIndex++;

            // ===> VERTEX STAGE <===

            VkPipelineVertexInputStateCreateInfo vertexInputInfo = VkPipelineVertexInputStateCreateInfo.callocStack(stack);
            vertexInputInfo.sType(VK_STRUCTURE_TYPE_PIPELINE_VERTEX_INPUT_STATE_CREATE_INFO);
            vertexInputInfo.pVertexBindingDescriptions(getVertexBindingDescriptions(mesh));
            vertexInputInfo.pVertexAttributeDescriptions(getVertexAttributeDescriptions(mesh));

            // ===> ASSEMBLY STAGE <===

            VkPipelineInputAssemblyStateCreateInfo inputAssembly = VkPipelineInputAssemblyStateCreateInfo.callocStack(stack);
            inputAssembly.sType(VK_STRUCTURE_TYPE_PIPELINE_INPUT_ASSEMBLY_STATE_CREATE_INFO);
            inputAssembly.topology(mesh.getTopology());
            inputAssembly.primitiveRestartEnable(false);

            // ===> VIEWPORT & SCISSOR

            VkViewport.Buffer viewport = VkViewport.callocStack(1, stack);
            viewport.x(0);
            viewport.y(0);
            VkExtent2D swapChainExtent = application.getSwapChain().getExtent();
            viewport.width(swapChainExtent.width());
            viewport.height(swapChainExtent.height());
            viewport.minDepth(0);
            viewport.maxDepth(1);

            VkRect2D.Buffer scissor = VkRect2D.callocStack(1, stack);
            scissor.offset(VkOffset2D.callocStack(stack).set(0, 0));
            scissor.extent(swapChainExtent);

            VkPipelineViewportStateCreateInfo viewportState = VkPipelineViewportStateCreateInfo.callocStack(stack);
            viewportState.sType(VK_STRUCTURE_TYPE_PIPELINE_VIEWPORT_STATE_CREATE_INFO);
            viewportState.pViewports(viewport);
            viewportState.pScissors(scissor);

            // ===> RASTERIZATION STAGE <===

            VkPipelineRasterizationStateCreateInfo rasterizer = VkPipelineRasterizationStateCreateInfo.callocStack(stack);
            rasterizer.sType(VK_STRUCTURE_TYPE_PIPELINE_RASTERIZATION_STATE_CREATE_INFO);
            rasterizer.depthClampEnable(false);
            rasterizer.rasterizerDiscardEnable(false);
            rasterizer.polygonMode(material.getFillMode());
            rasterizer.lineWidth(1);
            rasterizer.cullMode(material.getCullMode());
            rasterizer.frontFace(VK_FRONT_FACE_COUNTER_CLOCKWISE);
            rasterizer.depthBiasEnable(false);

            // ===> MULTISAMPLING <===

            VkPipelineMultisampleStateCreateInfo multisampling = VkPipelineMultisampleStateCreateInfo.callocStack(stack);
            multisampling.sType(VK_STRUCTURE_TYPE_PIPELINE_MULTISAMPLE_STATE_CREATE_INFO);
            multisampling.sampleShadingEnable(true);
            multisampling.minSampleShading(0.2f);
            multisampling.rasterizationSamples(application.getMsaaSamples());

            // ===> DEPTH STENCIL <===

            // TODO: Only append this whole state if needed? Check if it can be omitted it in some combinations
            VkPipelineDepthStencilStateCreateInfo depthStencil = VkPipelineDepthStencilStateCreateInfo.callocStack(stack);
            depthStencil.sType(VK_STRUCTURE_TYPE_PIPELINE_DEPTH_STENCIL_STATE_CREATE_INFO);
            depthStencil.depthTestEnable(material.isDepthTest());
            depthStencil.depthWriteEnable(material.isDepthWrite());
            depthStencil.depthCompareOp(VK_COMPARE_OP_LESS);
            depthStencil.depthBoundsTestEnable(false);
            depthStencil.minDepthBounds(0); // Optional
            depthStencil.maxDepthBounds(1); // Optional
            depthStencil.stencilTestEnable(false);

            // ===> COLOR BLENDING <===

            VkPipelineColorBlendAttachmentState.Buffer colorBlendAttachment = VkPipelineColorBlendAttachmentState.callocStack(1, stack);
            colorBlendAttachment.colorWriteMask(VK_COLOR_COMPONENT_R_BIT | VK_COLOR_COMPONENT_G_BIT | VK_COLOR_COMPONENT_B_BIT | VK_COLOR_COMPONENT_A_BIT);
            colorBlendAttachment.blendEnable(material.isTransparent());
            if (material.isTransparent()) {
                colorBlendAttachment.srcColorBlendFactor(VK_BLEND_FACTOR_SRC_ALPHA);
                colorBlendAttachment.dstColorBlendFactor(VK_BLEND_FACTOR_ONE_MINUS_SRC_ALPHA);
                colorBlendAttachment.colorBlendOp(VK_BLEND_OP_ADD);
                colorBlendAttachment.srcAlphaBlendFactor(VK_BLEND_FACTOR_ONE);
                colorBlendAttachment.dstAlphaBlendFactor(VK_BLEND_FACTOR_ZERO);
                colorBlendAttachment.alphaBlendOp(VK_BLEND_OP_ADD);
            }

            VkPipelineColorBlendStateCreateInfo colorBlending = VkPipelineColorBlendStateCreateInfo.callocStack(stack);
            colorBlending.sType(VK_STRUCTURE_TYPE_PIPELINE_COLOR_BLEND_STATE_CREATE_INFO);
            colorBlending.logicOpEnable(false);
            colorBlending.logicOp(VK_LOGIC_OP_COPY);
            colorBlending.pAttachments(colorBlendAttachment);
            colorBlending.blendConstants(stack.floats(0, 0, 0, 0));

            // ===> TESSELATION <===

            VkPipelineTessellationStateCreateInfo tesselation = null;
            if (material.getTesselationPatchSize() > 0) {
                tesselation = VkPipelineTessellationStateCreateInfo.callocStack(stack);
                tesselation.sType(VK_STRUCTURE_TYPE_PIPELINE_TESSELLATION_STATE_CREATE_INFO);
                tesselation.patchControlPoints(material.getTesselationPatchSize());
            }

            // ===> PIPELINE LAYOUT CREATION <===

            VkPipelineLayoutCreateInfo pipelineLayoutInfo = VkPipelineLayoutCreateInfo.callocStack(stack);
            pipelineLayoutInfo.sType(VK_STRUCTURE_TYPE_PIPELINE_LAYOUT_CREATE_INFO);
            pipelineLayoutInfo.pSetLayouts(stack.longs(materialDescriptorSet.getSetLayout().getDescriptorSetLayout()));

            LongBuffer pPipelineLayout = stack.longs(VK_NULL_HANDLE);
            if (vkCreatePipelineLayout(application.getLogicalDevice(), pipelineLayoutInfo, null, pPipelineLayout) != VK_SUCCESS) {
                throw new RuntimeException("Failed to create pipeline layout");
            }
            pipelineLayout = pPipelineLayout.get(0);

            VkGraphicsPipelineCreateInfo.Buffer pipelineInfo = VkGraphicsPipelineCreateInfo.callocStack(1, stack);
            pipelineInfo.sType(VK_STRUCTURE_TYPE_GRAPHICS_PIPELINE_CREATE_INFO);
            pipelineInfo.pStages(shaderStages);
            pipelineInfo.pVertexInputState(vertexInputInfo);
            pipelineInfo.pInputAssemblyState(inputAssembly);
            pipelineInfo.pViewportState(viewportState);
            pipelineInfo.pRasterizationState(rasterizer);
            pipelineInfo.pMultisampleState(multisampling);
            pipelineInfo.pDepthStencilState(depthStencil);
            pipelineInfo.pColorBlendState(colorBlending);
            pipelineInfo.pTessellationState(tesselation);
            pipelineInfo.layout(pipelineLayout);
            pipelineInfo.renderPass(renderJob.getRenderPass());
            pipelineInfo.subpass(0);
            pipelineInfo.basePipelineHandle(VK_NULL_HANDLE);
            pipelineInfo.basePipelineIndex(-1);

            LongBuffer pGraphicsPipeline = stack.mallocLong(1);
            if (vkCreateGraphicsPipelines(application.getLogicalDevice(), VK_NULL_HANDLE, pipelineInfo, null, pGraphicsPipeline) != VK_SUCCESS) {
                throw new RuntimeException("Failed to create graphics pipeline");
            }
            pipeline = pGraphicsPipeline.get(0);

            // ===> RELEASE RESOURCES <===

            for (long shaderModule : shaderModules) {
                vkDestroyShaderModule(application.getLogicalDevice(), shaderModule, null);
            }
        }
    }
}
