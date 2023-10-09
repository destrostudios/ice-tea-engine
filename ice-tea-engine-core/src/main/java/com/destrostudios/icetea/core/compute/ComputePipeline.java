package com.destrostudios.icetea.core.compute;

import com.destrostudios.icetea.core.*;
import com.destrostudios.icetea.core.shader.Shader;
import com.destrostudios.icetea.core.shader.ShaderType;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VkComputePipelineCreateInfo;
import org.lwjgl.vulkan.VkPipelineLayoutCreateInfo;
import org.lwjgl.vulkan.VkPipelineShaderStageCreateInfo;
import org.lwjgl.vulkan.VkPushConstantRange;

import java.nio.LongBuffer;

import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.vulkan.VK10.*;

public class ComputePipeline extends Pipeline {

    public ComputePipeline(ComputeActionGroup computeActionGroup) {
        this.computeActionGroup = computeActionGroup;
    }
    private ComputeActionGroup computeActionGroup;

    @Override
    protected void initNative() {
        super.initNative();
        try (MemoryStack stack = stackPush()) {
            ComputeAction referenceComputeAction = computeActionGroup.getComputeActions().get(0);
            Shader compShader = computeActionGroup.getComputeShader();
            long compShaderModule = application.getShaderManager().createShaderModule(compShader, ShaderType.COMPUTE_SHADER, referenceComputeAction.getResourceDescriptorSet().getShaderDeclaration());

            VkPipelineShaderStageCreateInfo compShaderStageInfo = VkPipelineShaderStageCreateInfo.callocStack(stack);
            compShaderStageInfo.sType(VK_STRUCTURE_TYPE_PIPELINE_SHADER_STAGE_CREATE_INFO);
            compShaderStageInfo.stage(VK_SHADER_STAGE_COMPUTE_BIT);
            compShaderStageInfo.module(compShaderModule);
            compShaderStageInfo.pName(stack.UTF8("main"));

            // ===> PIPELINE LAYOUT CREATION <===

            VkPipelineLayoutCreateInfo pipelineLayoutInfo = VkPipelineLayoutCreateInfo.callocStack(stack);
            pipelineLayoutInfo.sType(VK_STRUCTURE_TYPE_PIPELINE_LAYOUT_CREATE_INFO);
            pipelineLayoutInfo.pSetLayouts(referenceComputeAction.getResourceDescriptorSet().getDescriptorSetLayouts(stack));
            if (computeActionGroup.getPushConstantsSize() > 0) {
                VkPushConstantRange.Buffer pushConstantRange = VkPushConstantRange.calloc(1)
                        .stageFlags(VK_SHADER_STAGE_COMPUTE_BIT)
                        .size(computeActionGroup.getPushConstantsSize())
                        .offset(0);
                pipelineLayoutInfo.pPushConstantRanges(pushConstantRange);
            }

            LongBuffer pPipelineLayout = stack.longs(VK_NULL_HANDLE);
            int result = vkCreatePipelineLayout(application.getLogicalDevice(), pipelineLayoutInfo, null, pPipelineLayout);
            if (result != VK_SUCCESS) {
                throw new RuntimeException("Failed to create pipeline layout (result = " + result + ")");
            }
            pipelineLayout = pPipelineLayout.get(0);

            // ===> COMPUTE PIPELINE CREATION <===

            VkComputePipelineCreateInfo.Buffer computePipelineCreateInfo = VkComputePipelineCreateInfo.calloc(1)
                    .sType(VK_STRUCTURE_TYPE_COMPUTE_PIPELINE_CREATE_INFO)
                    .stage(compShaderStageInfo)
                    .layout(pipelineLayout);

            LongBuffer pComputePipeline = stack.mallocLong(1);
            result = vkCreateComputePipelines(application.getLogicalDevice(), VK_NULL_HANDLE, computePipelineCreateInfo, null, pComputePipeline);
            if (result != VK_SUCCESS) {
                throw new RuntimeException("Failed to create graphics pipeline (result = " + result + ")");
            }
            pipeline = pComputePipeline.get(0);

            // ===> RELEASE RESOURCES <===

            vkDestroyShaderModule(application.getLogicalDevice(), compShaderModule, null);
        }
    }
}
