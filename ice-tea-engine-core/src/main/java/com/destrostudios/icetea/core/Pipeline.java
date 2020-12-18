package com.destrostudios.icetea.core;

import lombok.Getter;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VkPipelineShaderStageCreateInfo;
import org.lwjgl.vulkan.VkShaderModuleCreateInfo;

import java.nio.ByteBuffer;
import java.nio.LongBuffer;

import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.vulkan.VK10.*;

public abstract class Pipeline {

    public Pipeline(Application application) {
        this.application = application;
    }
    protected Application application;
    @Getter
    protected long pipelineLayout;
    @Getter
    protected long pipeline;

    public abstract void init();

    protected void createShaderStage(VkPipelineShaderStageCreateInfo.Buffer shaderStages, int shaderStageIndex, int shaderStage, long shaderModule, MemoryStack stack) {
        VkPipelineShaderStageCreateInfo shaderStageCreateInfo = shaderStages.get(shaderStageIndex);
        shaderStageCreateInfo.sType(VK_STRUCTURE_TYPE_PIPELINE_SHADER_STAGE_CREATE_INFO);
        shaderStageCreateInfo.stage(shaderStage);
        shaderStageCreateInfo.module(shaderModule);
        shaderStageCreateInfo.pName(stack.UTF8("main"));
    }

    protected long createShaderModule(Application application, ByteBuffer spirvCode) {
        try (MemoryStack stack = stackPush()) {
            VkShaderModuleCreateInfo shaderModuleCreateInfo = VkShaderModuleCreateInfo.callocStack(stack);
            shaderModuleCreateInfo.sType(VK_STRUCTURE_TYPE_SHADER_MODULE_CREATE_INFO);
            shaderModuleCreateInfo.pCode(spirvCode);

            LongBuffer pShaderModule = stack.mallocLong(1);
            if (vkCreateShaderModule(application.getLogicalDevice(), shaderModuleCreateInfo, null, pShaderModule) != VK_SUCCESS) {
                throw new RuntimeException("Failed to create shader module");
            }
            return pShaderModule.get(0);
        }
    }

    public void cleanup() {
        vkDestroyPipeline(application.getLogicalDevice(), pipeline, null);
        vkDestroyPipelineLayout(application.getLogicalDevice(), pipelineLayout, null);
    }
}
