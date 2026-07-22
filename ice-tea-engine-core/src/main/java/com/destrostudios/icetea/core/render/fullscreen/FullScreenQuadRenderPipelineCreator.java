package com.destrostudios.icetea.core.render.fullscreen;

import com.destrostudios.icetea.core.Application;
import com.destrostudios.icetea.core.Pipeline;
import com.destrostudios.icetea.core.render.GeometryRenderContext;
import com.destrostudios.icetea.core.render.RenderPipelineCreator;
import com.destrostudios.icetea.core.resource.ResourceDescriptorSet;
import com.destrostudios.icetea.core.shader.FileShader;
import com.destrostudios.icetea.core.shader.Shader;
import com.destrostudios.icetea.core.shader.ShaderManager;
import com.destrostudios.icetea.core.shader.ShaderType;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.*;

import java.nio.LongBuffer;

import static org.lwjgl.vulkan.VK10.*;

public class FullScreenQuadRenderPipelineCreator extends RenderPipelineCreator<FullScreenQuadRenderJob, FullScreenQuadRenderPipelineState> {

    private static final Shader VERT_SHADER = new FileShader("com/destrostudios/icetea/core/shaders/fullScreenQuad.vert");

    public FullScreenQuadRenderPipelineCreator(Application application, FullScreenQuadRenderJob renderJob) {
        super(application, renderJob);
    }

    @Override
    protected FullScreenQuadRenderPipelineState createState(GeometryRenderContext<FullScreenQuadRenderJob> geometryRenderContext) {
        throw new UnsupportedOperationException();
    }

    @Override
    protected Pipeline createPipeline(FullScreenQuadRenderPipelineState state, LongBuffer descriptorSetLayouts, MemoryStack stack) {
        ResourceDescriptorSet resourceDescriptorSet = renderJob.getResourceDescriptorSet();
        String resourceDescriptorSetShaderDeclaration = resourceDescriptorSet.getShaderDeclaration();

        ShaderManager shaderManager = application.getShaderManager();

        VkPipelineShaderStageCreateInfo.Buffer shaderStages = VkPipelineShaderStageCreateInfo.calloc(2, stack);

        long vertShaderModule = createShaderModule(VERT_SHADER, ShaderType.VERTEX_SHADER, resourceDescriptorSetShaderDeclaration);
        shaderManager.createShaderStage(shaderStages, 0, VK_SHADER_STAGE_VERTEX_BIT, vertShaderModule, stack);

        long fragShaderModule = createShaderModule(renderJob.getFragmentShader(), ShaderType.FRAGMENT_SHADER, resourceDescriptorSetShaderDeclaration);
        shaderManager.createShaderStage(shaderStages, 1, VK_SHADER_STAGE_FRAGMENT_BIT, fragShaderModule, stack);

        // ===> VERTEX STAGE <===

        VkPipelineVertexInputStateCreateInfo vertexInputInfo = VkPipelineVertexInputStateCreateInfo.calloc(stack);
        vertexInputInfo.sType(VK_STRUCTURE_TYPE_PIPELINE_VERTEX_INPUT_STATE_CREATE_INFO);
        // No vertex binding or vertex attribute descriptions needed

        // ===> ASSEMBLY STAGE <===

        VkPipelineInputAssemblyStateCreateInfo inputAssembly = VkPipelineInputAssemblyStateCreateInfo.calloc(stack);
        inputAssembly.sType(VK_STRUCTURE_TYPE_PIPELINE_INPUT_ASSEMBLY_STATE_CREATE_INFO);
        inputAssembly.topology(VK_PRIMITIVE_TOPOLOGY_TRIANGLE_LIST);

        // ===> VIEWPORT & SCISSOR

        VkViewport.Buffer viewport = VkViewport.calloc(1, stack);
        viewport.x(0);
        viewport.y(0);
        viewport.width(renderJob.getExtent().width());
        viewport.height(renderJob.getExtent().height());
        viewport.minDepth(0);
        viewport.maxDepth(1);

        VkRect2D.Buffer scissor = VkRect2D.calloc(1, stack);
        scissor.offset(VkOffset2D.calloc(stack).set(0, 0));
        scissor.extent(renderJob.getExtent());

        VkPipelineViewportStateCreateInfo viewportState = VkPipelineViewportStateCreateInfo.calloc(stack);
        viewportState.sType(VK_STRUCTURE_TYPE_PIPELINE_VIEWPORT_STATE_CREATE_INFO);
        viewportState.pViewports(viewport);
        viewportState.pScissors(scissor);

        // ===> RASTERIZATION STAGE <===

        VkPipelineRasterizationStateCreateInfo rasterizer = VkPipelineRasterizationStateCreateInfo.calloc(stack);
        rasterizer.sType(VK_STRUCTURE_TYPE_PIPELINE_RASTERIZATION_STATE_CREATE_INFO);
        rasterizer.polygonMode(VK_POLYGON_MODE_FILL);
        rasterizer.lineWidth(1);
        rasterizer.cullMode(VK_CULL_MODE_FRONT_BIT);
        rasterizer.frontFace(VK_FRONT_FACE_COUNTER_CLOCKWISE);

        // ===> MULTISAMPLING <===

        VkPipelineMultisampleStateCreateInfo multisampling = VkPipelineMultisampleStateCreateInfo.calloc(stack);
        multisampling.sType(VK_STRUCTURE_TYPE_PIPELINE_MULTISAMPLE_STATE_CREATE_INFO);
        multisampling.sampleShadingEnable(true);
        multisampling.minSampleShading(0.2f);
        multisampling.rasterizationSamples(application.getMsaaSamples());

        // ===> COLOR BLENDING <===

        VkPipelineColorBlendAttachmentState.Buffer colorBlendAttachment = VkPipelineColorBlendAttachmentState.calloc(1, stack);
        colorBlendAttachment.colorWriteMask(VK_COLOR_COMPONENT_R_BIT | VK_COLOR_COMPONENT_G_BIT | VK_COLOR_COMPONENT_B_BIT | VK_COLOR_COMPONENT_A_BIT);

        VkPipelineColorBlendStateCreateInfo colorBlending = VkPipelineColorBlendStateCreateInfo.calloc(stack);
        colorBlending.sType(VK_STRUCTURE_TYPE_PIPELINE_COLOR_BLEND_STATE_CREATE_INFO);
        colorBlending.pAttachments(colorBlendAttachment);

        // ===> PIPELINE LAYOUT CREATION <===

        VkPipelineLayoutCreateInfo pipelineLayoutInfo = VkPipelineLayoutCreateInfo.calloc(stack);
        pipelineLayoutInfo.sType(VK_STRUCTURE_TYPE_PIPELINE_LAYOUT_CREATE_INFO);
        pipelineLayoutInfo.pSetLayouts(descriptorSetLayouts);

        LongBuffer pPipelineLayout = stack.longs(VK_NULL_HANDLE);
        int result = vkCreatePipelineLayout(application.getLogicalDevice(), pipelineLayoutInfo, null, pPipelineLayout);
        if (result != VK_SUCCESS) {
            throw new RuntimeException("Failed to create pipeline layout (result = " + result + ")");
        }
        long pipelineLayout = pPipelineLayout.get(0);

        VkGraphicsPipelineCreateInfo.Buffer pipelineInfo = VkGraphicsPipelineCreateInfo.calloc(1, stack);
        pipelineInfo.sType(VK_STRUCTURE_TYPE_GRAPHICS_PIPELINE_CREATE_INFO);
        pipelineInfo.pStages(shaderStages);
        pipelineInfo.pVertexInputState(vertexInputInfo);
        pipelineInfo.pInputAssemblyState(inputAssembly);
        pipelineInfo.pViewportState(viewportState);
        pipelineInfo.pRasterizationState(rasterizer);
        pipelineInfo.pMultisampleState(multisampling);
        pipelineInfo.pColorBlendState(colorBlending);
        pipelineInfo.layout(pipelineLayout);
        pipelineInfo.renderPass(renderJob.getRenderPass());

        LongBuffer pGraphicsPipeline = stack.mallocLong(1);
        result = vkCreateGraphicsPipelines(application.getLogicalDevice(), VK_NULL_HANDLE, pipelineInfo, null, pGraphicsPipeline);
        if (result != VK_SUCCESS) {
            throw new RuntimeException("Failed to create graphics pipeline (result = " + result + ")");
        }
        long pipeline = pGraphicsPipeline.get(0);

        // ===> RELEASE RESOURCES <===

        vkDestroyShaderModule(application.getLogicalDevice(), vertShaderModule, null);
        vkDestroyShaderModule(application.getLogicalDevice(), fragShaderModule, null);

        return new Pipeline(pipelineLayout, pipeline);
    }
}
