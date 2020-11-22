package com.destrostudios.icetea.core;

import lombok.Getter;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.*;

import java.util.List;

import static org.lwjgl.vulkan.VK10.vkDestroyFramebuffer;
import static org.lwjgl.vulkan.VK10.vkDestroyRenderPass;

public abstract class RenderJob<GRC extends GeometryRenderContext<?>> {

    protected Application application;
    @Getter
    protected VkExtent2D extent;
    @Getter
    protected long renderPass;
    protected List<Long> frameBuffers;

    public boolean isInitialized() {
        return (application != null);
    }

    public void init(Application application) {
        this.application = application;
        extent = calculateExtent();
    }

    protected abstract VkExtent2D calculateExtent();

    public void updateUniformBuffers(int currentImage, MemoryStack stack) {

    }

    public abstract GRC createGeometryRenderContext();

    public VkRect2D getRenderArea(MemoryStack stack) {
        VkRect2D renderArea = VkRect2D.callocStack(stack);
        renderArea.offset(VkOffset2D.callocStack(stack).set(0, 0));
        renderArea.extent(extent);
        return renderArea;
    }

    public abstract VkClearValue.Buffer getClearValues(MemoryStack stack);

    public abstract long getFramebuffer(int commandBufferIndex);

    public abstract void render(VkCommandBuffer commandBuffer, int commandBufferIndex, MemoryStack stack);

    public void cleanup() {
        if (isInitialized()) {
            frameBuffers.forEach(frameBuffer -> vkDestroyFramebuffer(application.getLogicalDevice(), frameBuffer, null));
            vkDestroyRenderPass(application.getLogicalDevice(), renderPass, null);
        }
    }
}
