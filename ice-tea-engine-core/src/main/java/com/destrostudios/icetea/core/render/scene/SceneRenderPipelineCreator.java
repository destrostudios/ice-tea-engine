package com.destrostudios.icetea.core.render.scene;

import com.destrostudios.icetea.core.Application;
import com.destrostudios.icetea.core.Pipeline;
import com.destrostudios.icetea.core.render.EssentialGeometryRenderPipelineCreator;
import com.destrostudios.icetea.core.render.GeometryRenderContext;
import com.destrostudios.icetea.core.scene.Geometry;
import com.destrostudios.icetea.core.shader.ShaderManager;
import com.destrostudios.icetea.core.shader.ShaderType;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.*;

import java.nio.LongBuffer;
import java.util.LinkedList;

import static org.lwjgl.vulkan.VK10.*;

public class SceneRenderPipelineCreator extends EssentialGeometryRenderPipelineCreator<SceneRenderJob, SceneRenderPipelineState> {

    public SceneRenderPipelineCreator(Application application, SceneRenderJob renderJob) {
        super(application, renderJob);
    }

    @Override
    protected SceneRenderPipelineState createState(GeometryRenderContext<SceneRenderJob> geometryRenderContext) {
        SceneRenderPipelineState state = new SceneRenderPipelineState("renderScene");
        fillEssentialGeometryState(state, geometryRenderContext);

        Geometry geometry = geometryRenderContext.getGeometry();
        state.setFragmentShader(geometry.getMaterial().getFragmentShader());

        return state;
    }

    @Override
    protected Pipeline createPipeline(SceneRenderPipelineState state, LongBuffer descriptorSetLayouts, MemoryStack stack) {
        int shaderStagesCount = 2;
        if (state.getTessellationControlShader() != null) {
            shaderStagesCount++;
        }
        if (state.getTessellationEvaluationShader() != null) {
            shaderStagesCount++;
        }
        if (state.getGeometryShader() != null) {
            shaderStagesCount++;
        }

        VkPipelineShaderStageCreateInfo.Buffer shaderStages = VkPipelineShaderStageCreateInfo.callocStack(shaderStagesCount, stack);

        ShaderManager shaderManager = application.getShaderManager();
        int shaderStageIndex = 0;
        LinkedList<Long> shaderModules = new LinkedList<>();

        long vertShaderModule = createShaderModule_Vertex(state.getVertexShader(), state.getDescriptorSetShaderDeclaration(), state.getVertexFields());
        shaderManager.createShaderStage(shaderStages, shaderStageIndex, VK_SHADER_STAGE_VERTEX_BIT, vertShaderModule, stack);
        shaderModules.add(vertShaderModule);
        shaderStageIndex++;

        if (state.getTessellationControlShader() != null) {
            long tessellationControlShaderModule = createShaderModule(state.getTessellationControlShader(), ShaderType.TESSELLATION_CONTROL_SHADER, state.getDescriptorSetShaderDeclaration());
            shaderManager.createShaderStage(shaderStages, shaderStageIndex, VK_SHADER_STAGE_TESSELLATION_CONTROL_BIT, tessellationControlShaderModule, stack);
            shaderModules.add(tessellationControlShaderModule);
            shaderStageIndex++;
        }

        if (state.getTessellationEvaluationShader() != null) {
            long tessellationEvaluationShaderModule = createShaderModule(state.getTessellationEvaluationShader(), ShaderType.TESSELLATION_EVALUATION_SHADER, state.getDescriptorSetShaderDeclaration());
            shaderManager.createShaderStage(shaderStages, shaderStageIndex, VK_SHADER_STAGE_TESSELLATION_EVALUATION_BIT, tessellationEvaluationShaderModule, stack);
            shaderModules.add(tessellationEvaluationShaderModule);
            shaderStageIndex++;
        }

        if (state.getGeometryShader() != null) {
            long geometryShaderModule = createShaderModule(state.getGeometryShader(), ShaderType.GEOMETRY_SHADER, state.getDescriptorSetShaderDeclaration());
            shaderManager.createShaderStage(shaderStages, shaderStageIndex, VK_SHADER_STAGE_GEOMETRY_BIT, geometryShaderModule, stack);
            shaderModules.add(geometryShaderModule);
            shaderStageIndex++;
        }

        long fragShaderModule = createShaderModule(state.getFragmentShader(), ShaderType.FRAGMENT_SHADER, state.getDescriptorSetShaderDeclaration());
        shaderManager.createShaderStage(shaderStages, shaderStageIndex, VK_SHADER_STAGE_FRAGMENT_BIT, fragShaderModule, stack);
        shaderModules.add(fragShaderModule);
        shaderStageIndex++;

        // ===> VERTEX STAGE <===

        VkPipelineVertexInputStateCreateInfo vertexInputInfo = VkPipelineVertexInputStateCreateInfo.callocStack(stack);
        vertexInputInfo.sType(VK_STRUCTURE_TYPE_PIPELINE_VERTEX_INPUT_STATE_CREATE_INFO);
        vertexInputInfo.pVertexBindingDescriptions(getVertexBindingDescriptions(state.getVertexSize()));
        vertexInputInfo.pVertexAttributeDescriptions(getVertexAttributeDescriptions(state.getVertexFields()));

        // ===> ASSEMBLY STAGE <===

        VkPipelineInputAssemblyStateCreateInfo inputAssembly = VkPipelineInputAssemblyStateCreateInfo.callocStack(stack);
        inputAssembly.sType(VK_STRUCTURE_TYPE_PIPELINE_INPUT_ASSEMBLY_STATE_CREATE_INFO);
        inputAssembly.topology(state.getTopology());
        inputAssembly.primitiveRestartEnable(false);

        // ===> DYNAMIC <===

        VkPipelineDynamicStateCreateInfo dynamic = null;
        if (state.getDynamicStates() != null) {
            dynamic = VkPipelineDynamicStateCreateInfo.callocStack(stack);
            dynamic.sType(VK_STRUCTURE_TYPE_PIPELINE_DYNAMIC_STATE_CREATE_INFO);
            dynamic.pDynamicStates(stack.ints(state.getDynamicStates()));
        }

        // ===> VIEWPORT & SCISSOR <===
        // TODO: The viewport state is not needed if the dynamic state is set and the renderer provides all dynamic input

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
        rasterizer.polygonMode(state.getPolygonMode());
        rasterizer.lineWidth(1);
        rasterizer.cullMode(state.getCullMode());
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
        depthStencil.depthTestEnable(state.isDepthTest());
        depthStencil.depthWriteEnable(state.isDepthWrite());
        depthStencil.depthCompareOp(VK_COMPARE_OP_LESS);
        depthStencil.depthBoundsTestEnable(false);
        depthStencil.minDepthBounds(0); // Optional
        depthStencil.maxDepthBounds(1); // Optional
        depthStencil.stencilTestEnable(false);

        // ===> COLOR BLENDING <===

        VkPipelineColorBlendAttachmentState.Buffer colorBlendAttachment = VkPipelineColorBlendAttachmentState.callocStack(1, stack);
        colorBlendAttachment.colorWriteMask(VK_COLOR_COMPONENT_R_BIT | VK_COLOR_COMPONENT_G_BIT | VK_COLOR_COMPONENT_B_BIT | VK_COLOR_COMPONENT_A_BIT);
        colorBlendAttachment.blendEnable(state.isTransparent());
        if (state.isTransparent()) {
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

        // ===> TESSELLATION <===

        VkPipelineTessellationStateCreateInfo tessellation = null;
        if (state.getTessellationPatchSize() > 0) {
            tessellation = VkPipelineTessellationStateCreateInfo.callocStack(stack);
            tessellation.sType(VK_STRUCTURE_TYPE_PIPELINE_TESSELLATION_STATE_CREATE_INFO);
            tessellation.patchControlPoints(state.getTessellationPatchSize());
        }

        // ===> PIPELINE LAYOUT CREATION <===

        VkPipelineLayoutCreateInfo pipelineLayoutInfo = VkPipelineLayoutCreateInfo.callocStack(stack);
        pipelineLayoutInfo.sType(VK_STRUCTURE_TYPE_PIPELINE_LAYOUT_CREATE_INFO);
        pipelineLayoutInfo.pSetLayouts(descriptorSetLayouts);

        LongBuffer pPipelineLayout = stack.longs(VK_NULL_HANDLE);
        int result = vkCreatePipelineLayout(application.getLogicalDevice(), pipelineLayoutInfo, null, pPipelineLayout);
        if (result != VK_SUCCESS) {
            throw new RuntimeException("Failed to create pipeline layout (result = " + result + ")");
        }
        long pipelineLayout = pPipelineLayout.get(0);

        VkGraphicsPipelineCreateInfo.Buffer pipelineInfo = VkGraphicsPipelineCreateInfo.callocStack(1, stack);
        pipelineInfo.sType(VK_STRUCTURE_TYPE_GRAPHICS_PIPELINE_CREATE_INFO);
        pipelineInfo.pStages(shaderStages);
        pipelineInfo.pVertexInputState(vertexInputInfo);
        pipelineInfo.pInputAssemblyState(inputAssembly);
        pipelineInfo.pDynamicState(dynamic);
        pipelineInfo.pViewportState(viewportState);
        pipelineInfo.pRasterizationState(rasterizer);
        pipelineInfo.pMultisampleState(multisampling);
        pipelineInfo.pDepthStencilState(depthStencil);
        pipelineInfo.pColorBlendState(colorBlending);
        pipelineInfo.pTessellationState(tessellation);
        pipelineInfo.layout(pipelineLayout);
        pipelineInfo.renderPass(renderJob.getRenderPass());
        pipelineInfo.subpass(0);
        pipelineInfo.basePipelineHandle(VK_NULL_HANDLE);
        pipelineInfo.basePipelineIndex(-1);

        LongBuffer pGraphicsPipeline = stack.mallocLong(1);
        result = vkCreateGraphicsPipelines(application.getLogicalDevice(), VK_NULL_HANDLE, pipelineInfo, null, pGraphicsPipeline);
        if (result != VK_SUCCESS) {
            throw new RuntimeException("Failed to create graphics pipeline (result = " + result + ")");
        }
        long pipeline = pGraphicsPipeline.get(0);

        // ===> RELEASE RESOURCES <===

        for (long shaderModule : shaderModules) {
            vkDestroyShaderModule(application.getLogicalDevice(), shaderModule, null);
        }

        return new Pipeline(pipelineLayout, pipeline);
    }
}
