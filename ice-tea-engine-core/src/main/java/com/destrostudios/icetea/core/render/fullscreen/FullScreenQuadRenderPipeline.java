package com.destrostudios.icetea.core.render.fullscreen;

import com.destrostudios.icetea.core.*;
import com.destrostudios.icetea.core.material.descriptor.MaterialDescriptorSet;
import com.destrostudios.icetea.core.render.RenderPipeline;
import com.destrostudios.icetea.core.shader.Shader;
import com.destrostudios.icetea.core.shader.ShaderType;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.*;

import java.nio.ByteBuffer;
import java.nio.LongBuffer;

import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.vulkan.VK10.*;

public class FullScreenQuadRenderPipeline extends RenderPipeline<FullScreenQuadRenderJob> {

    public FullScreenQuadRenderPipeline(Application application, FullScreenQuadRenderJob renderJob) {
        super(application, renderJob);
    }

    @Override
    public void init() {
        try (MemoryStack stack = stackPush()) {
            MaterialDescriptorSet materialDescriptorSet = renderJob.getMaterialDescriptorSet();
            String materialDescriptorSetShaderDeclaration = materialDescriptorSet.getShaderDeclaration();

            Shader vertShader = new Shader("shaders/fullScreenQuad.vert");
            Shader fragShader = renderJob.getFragmentShader();
            long vertShaderModule = createShaderModule(vertShader, ShaderType.VERTEX_SHADER, materialDescriptorSetShaderDeclaration);
            long fragShaderModule = createShaderModule(fragShader, ShaderType.FRAGMENT_SHADER, materialDescriptorSetShaderDeclaration);

            VkPipelineShaderStageCreateInfo.Buffer shaderStages = VkPipelineShaderStageCreateInfo.callocStack(2, stack);
            ByteBuffer entryPoint = stack.UTF8("main");

            VkPipelineShaderStageCreateInfo vertShaderStageInfo = shaderStages.get(0);
            vertShaderStageInfo.sType(VK_STRUCTURE_TYPE_PIPELINE_SHADER_STAGE_CREATE_INFO);
            vertShaderStageInfo.stage(VK_SHADER_STAGE_VERTEX_BIT);
            vertShaderStageInfo.module(vertShaderModule);
            vertShaderStageInfo.pName(entryPoint);

            VkPipelineShaderStageCreateInfo fragShaderStageInfo = shaderStages.get(1);
            fragShaderStageInfo.sType(VK_STRUCTURE_TYPE_PIPELINE_SHADER_STAGE_CREATE_INFO);
            fragShaderStageInfo.stage(VK_SHADER_STAGE_FRAGMENT_BIT);
            fragShaderStageInfo.module(fragShaderModule);
            fragShaderStageInfo.pName(entryPoint);

            // ===> VERTEX STAGE <===

            VkPipelineVertexInputStateCreateInfo vertexInputInfo = VkPipelineVertexInputStateCreateInfo.callocStack(stack);
            vertexInputInfo.sType(VK_STRUCTURE_TYPE_PIPELINE_VERTEX_INPUT_STATE_CREATE_INFO);
            // No vertex binding or vertex attribute descriptions needed

            // ===> ASSEMBLY STAGE <===

            VkPipelineInputAssemblyStateCreateInfo inputAssembly = VkPipelineInputAssemblyStateCreateInfo.callocStack(stack);
            inputAssembly.sType(VK_STRUCTURE_TYPE_PIPELINE_INPUT_ASSEMBLY_STATE_CREATE_INFO);
            inputAssembly.topology(VK_PRIMITIVE_TOPOLOGY_TRIANGLE_LIST);
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
            rasterizer.polygonMode(VK_POLYGON_MODE_FILL);
            rasterizer.lineWidth(1);
            rasterizer.cullMode(VK_CULL_MODE_FRONT_BIT);
            rasterizer.frontFace(VK_FRONT_FACE_COUNTER_CLOCKWISE);
            rasterizer.depthBiasEnable(false);

            // ===> MULTISAMPLING <===

            VkPipelineMultisampleStateCreateInfo multisampling = VkPipelineMultisampleStateCreateInfo.callocStack(stack);
            multisampling.sType(VK_STRUCTURE_TYPE_PIPELINE_MULTISAMPLE_STATE_CREATE_INFO);
            multisampling.sampleShadingEnable(true);
            multisampling.minSampleShading(0.2f);
            multisampling.rasterizationSamples(application.getMsaaSamples());

            // ===> DEPTH STENCIL <===

            VkPipelineDepthStencilStateCreateInfo depthStencil = VkPipelineDepthStencilStateCreateInfo.callocStack(stack);
            depthStencil.sType(VK_STRUCTURE_TYPE_PIPELINE_DEPTH_STENCIL_STATE_CREATE_INFO);
            depthStencil.depthTestEnable(true);
            depthStencil.depthWriteEnable(true);
            depthStencil.depthCompareOp(VK_COMPARE_OP_LESS);
            depthStencil.depthBoundsTestEnable(false);
            depthStencil.minDepthBounds(0); // Optional
            depthStencil.maxDepthBounds(1); // Optional
            depthStencil.stencilTestEnable(false);

            // ===> COLOR BLENDING <===

            VkPipelineColorBlendAttachmentState.Buffer colorBlendAttachment = VkPipelineColorBlendAttachmentState.callocStack(1, stack);
            colorBlendAttachment.colorWriteMask(VK_COLOR_COMPONENT_R_BIT | VK_COLOR_COMPONENT_G_BIT | VK_COLOR_COMPONENT_B_BIT | VK_COLOR_COMPONENT_A_BIT);

            VkPipelineColorBlendStateCreateInfo colorBlending = VkPipelineColorBlendStateCreateInfo.callocStack(stack);
            colorBlending.sType(VK_STRUCTURE_TYPE_PIPELINE_COLOR_BLEND_STATE_CREATE_INFO);
            colorBlending.logicOpEnable(false);
            colorBlending.logicOp(VK_LOGIC_OP_COPY);
            colorBlending.pAttachments(colorBlendAttachment);
            colorBlending.blendConstants(stack.floats(0, 0, 0, 0));

            // ===> PIPELINE LAYOUT CREATION <===

            VkPipelineLayoutCreateInfo pipelineLayoutInfo = VkPipelineLayoutCreateInfo.callocStack(stack);
            pipelineLayoutInfo.sType(VK_STRUCTURE_TYPE_PIPELINE_LAYOUT_CREATE_INFO);
            pipelineLayoutInfo.pSetLayouts(stack.longs(materialDescriptorSet.getSetLayout().getDescriptorSetLayout()));

            LongBuffer pPipelineLayout = stack.longs(VK_NULL_HANDLE);
            int result = vkCreatePipelineLayout(application.getLogicalDevice(), pipelineLayoutInfo, null, pPipelineLayout);
            if (result != VK_SUCCESS) {
                throw new RuntimeException("Failed to create pipeline layout (result = " + result + ")");
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
            pipeline = pGraphicsPipeline.get(0);

            // ===> RELEASE RESOURCES <===

            vkDestroyShaderModule(application.getLogicalDevice(), vertShaderModule, null);
            vkDestroyShaderModule(application.getLogicalDevice(), fragShaderModule, null);
        }
    }
}
