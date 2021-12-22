package com.destrostudios.icetea.core;

import com.destrostudios.icetea.core.shader.Shader;
import com.destrostudios.icetea.core.shader.ShaderType;
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

    protected long createShaderModule(Shader shader, ShaderType shaderType, String additionalDeclarations) {
        try (MemoryStack stack = stackPush()) {
            ByteBuffer compiledShaderCode = application.getShaderManager().getCompiledShaderCode(shader, shaderType, additionalDeclarations);

            VkShaderModuleCreateInfo shaderModuleCreateInfo = VkShaderModuleCreateInfo.callocStack(stack);
            shaderModuleCreateInfo.sType(VK_STRUCTURE_TYPE_SHADER_MODULE_CREATE_INFO);
            shaderModuleCreateInfo.pCode(compiledShaderCode);

            LongBuffer pShaderModule = stack.mallocLong(1);
            int result = vkCreateShaderModule(application.getLogicalDevice(), shaderModuleCreateInfo, null, pShaderModule);
            if (result != VK_SUCCESS) {
                throw new RuntimeException("Failed to create shader module (result = " + result + ")");
            }
            return pShaderModule.get(0);
        }
    }

    public void cleanup() {
        vkDestroyPipeline(application.getLogicalDevice(), pipeline, null);
        vkDestroyPipelineLayout(application.getLogicalDevice(), pipelineLayout, null);
    }
}
