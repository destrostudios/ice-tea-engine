package com.destrostudios.icetea.core;

import lombok.Getter;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.*;

import java.nio.ByteBuffer;
import java.nio.LongBuffer;

import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.vulkan.VK10.*;

public abstract class RenderPipeline<RJ extends RenderJob<?>> {

    public RenderPipeline(Application application, RJ renderJob) {
        this.application = application;
        this.renderJob = renderJob;
    }
    protected Application application;
    protected RJ renderJob;
    @Getter
    protected long pipelineLayout;
    @Getter
    protected long graphicsPipeline;

    public abstract void init();

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
        vkDestroyPipeline(application.getLogicalDevice(), graphicsPipeline, null);
        vkDestroyPipelineLayout(application.getLogicalDevice(), pipelineLayout, null);
    }
}
