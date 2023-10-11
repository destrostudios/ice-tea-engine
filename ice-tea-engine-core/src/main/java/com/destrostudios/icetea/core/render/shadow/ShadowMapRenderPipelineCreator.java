package com.destrostudios.icetea.core.render.shadow;

import com.destrostudios.icetea.core.Application;
import com.destrostudios.icetea.core.Pipeline;
import com.destrostudios.icetea.core.render.EssentialGeometryRenderPipelineCreator;
import com.destrostudios.icetea.core.render.GeometryRenderContext;
import com.destrostudios.icetea.core.shader.Shader;
import com.destrostudios.icetea.core.shader.ShaderManager;
import com.destrostudios.icetea.core.shader.ShaderType;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.*;

import java.nio.LongBuffer;
import java.util.LinkedList;

import static org.lwjgl.vulkan.VK10.*;

public class ShadowMapRenderPipelineCreator extends EssentialGeometryRenderPipelineCreator<ShadowMapRenderJob, ShadowMapRenderPipelineState> {

    public ShadowMapRenderPipelineCreator(Application application, ShadowMapRenderJob renderJob) {
        super(application, renderJob);
    }

    @Override
    protected ShadowMapRenderPipelineState createState(GeometryRenderContext<ShadowMapRenderJob> geometryRenderContext) {
        ShadowMapRenderPipelineState state = new ShadowMapRenderPipelineState("renderShadowMap");
        fillEssentialGeometryState(state, geometryRenderContext);
        return state;
    }

    @Override
    protected Pipeline createPipeline(ShadowMapRenderPipelineState state, LongBuffer descriptorSetLayouts, MemoryStack stack) {
        int shaderStagesCount = 1;
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

        Shader vertShader = new Shader("com/destrostudios/icetea/core/shaders/shadow.vert");
        long vertShaderModule = createShaderModule_Vertex(vertShader, state.getDescriptorSetShaderDeclaration(), state.getVertexFields());
        shaderManager.createShaderStage(shaderStages, shaderStageIndex, VK_SHADER_STAGE_VERTEX_BIT, vertShaderModule, stack);
        shaderModules.add(vertShaderModule);
        shaderStageIndex++;

        if (state.getTessellationControlShader() != null) {
            long tessellationControlShaderModule = shaderManager.createShaderModule(state.getTessellationControlShader(), ShaderType.TESSELLATION_CONTROL_SHADER, state.getDescriptorSetShaderDeclaration());
            shaderManager.createShaderStage(shaderStages, shaderStageIndex, VK_SHADER_STAGE_TESSELLATION_CONTROL_BIT, tessellationControlShaderModule, stack);
            shaderModules.add(tessellationControlShaderModule);
            shaderStageIndex++;
        }

        if (state.getTessellationEvaluationShader() != null) {
            long tessellationEvaluationShaderModule = shaderManager.createShaderModule(state.getTessellationEvaluationShader(), ShaderType.TESSELLATION_EVALUATION_SHADER, state.getDescriptorSetShaderDeclaration());
            shaderManager.createShaderStage(shaderStages, shaderStageIndex, VK_SHADER_STAGE_TESSELLATION_EVALUATION_BIT, tessellationEvaluationShaderModule, stack);
            shaderModules.add(tessellationEvaluationShaderModule);
            shaderStageIndex++;
        }

        if (state.getGeometryShader() != null) {
            long geometryShaderModule = shaderManager.createShaderModule(state.getGeometryShader(), ShaderType.GEOMETRY_SHADER, state.getDescriptorSetShaderDeclaration());
            shaderManager.createShaderStage(shaderStages, shaderStageIndex, VK_SHADER_STAGE_GEOMETRY_BIT, geometryShaderModule, stack);
            shaderModules.add(geometryShaderModule);
            shaderStageIndex++;
        }

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
        viewport.width(renderJob.getShadowConfig().getShadowMapSize());
        viewport.height(renderJob.getShadowConfig().getShadowMapSize());
        viewport.minDepth(0);
        viewport.maxDepth(1);

        VkRect2D.Buffer scissor = VkRect2D.callocStack(1, stack);
        scissor.offset(VkOffset2D.callocStack(stack).set(0, 0));
        scissor.extent(renderJob.getExtent());

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
        rasterizer.cullMode(VK_CULL_MODE_BACK_BIT);
        rasterizer.frontFace(VK_FRONT_FACE_COUNTER_CLOCKWISE);
        rasterizer.depthBiasEnable(true);
        // TODO: Make these configurable
        rasterizer.depthBiasConstantFactor(4.0f);
        rasterizer.depthBiasSlopeFactor(1.5f);

        // ===> MULTISAMPLING <===

        VkPipelineMultisampleStateCreateInfo multisampling = VkPipelineMultisampleStateCreateInfo.callocStack(stack);
        multisampling.sType(VK_STRUCTURE_TYPE_PIPELINE_MULTISAMPLE_STATE_CREATE_INFO);
        multisampling.rasterizationSamples(VK_SAMPLE_COUNT_1_BIT);
        multisampling.flags(0);

        // ===> DEPTH STENCIL <===

        VkPipelineDepthStencilStateCreateInfo depthStencil = VkPipelineDepthStencilStateCreateInfo.callocStack(stack);
        depthStencil.sType(VK_STRUCTURE_TYPE_PIPELINE_DEPTH_STENCIL_STATE_CREATE_INFO);
        depthStencil.depthTestEnable(true);
        depthStencil.depthWriteEnable(true);
        depthStencil.depthCompareOp(VK_COMPARE_OP_LESS_OR_EQUAL);
        depthStencil.back().compareOp(VK_COMPARE_OP_ALWAYS);

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
        VkPushConstantRange.Buffer pushConstantRange = VkPushConstantRange.calloc(1)
                .stageFlags(VK_SHADER_STAGE_VERTEX_BIT)
                .size(renderJob.getPushConstants().getData().getSize())
                .offset(0);
        pipelineLayoutInfo.pPushConstantRanges(pushConstantRange);

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
        pipelineInfo.pViewportState(viewportState);
        pipelineInfo.pRasterizationState(rasterizer);
        pipelineInfo.pMultisampleState(multisampling);
        pipelineInfo.pDepthStencilState(depthStencil);
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
