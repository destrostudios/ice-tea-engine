package com.destrostudios.icetea.core.render;

import com.destrostudios.icetea.core.texture.Texture;
import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;

import java.nio.LongBuffer;

import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.util.vma.Vma.VMA_MEMORY_USAGE_AUTO_PREFER_DEVICE;
import static org.lwjgl.vulkan.VK10.*;
import static org.lwjgl.vulkan.VK10.VK_IMAGE_LAYOUT_COLOR_ATTACHMENT_OPTIMAL;

public abstract class MultisampleRenderJob<RPC extends RenderPipelineCreator<?, ?>> extends RenderJob<RPC> {

    public MultisampleRenderJob(String name) {
        super(name);
    }

    protected void initMultisampledColorTexture(Texture texture) {
        try (MemoryStack stack = stackPush()) {
            int imageFormat = application.getSwapChain().getImageFormat();

            LongBuffer pColorImage = stack.mallocLong(1);
            PointerBuffer pColorImageAllocation = stack.mallocPointer(1);
            application.getImageManager().createImage(
                    extent.width(),
                    extent.height(),
                    1,
                    application.getMsaaSamples(),
                    imageFormat,
                    VK_IMAGE_USAGE_COLOR_ATTACHMENT_BIT | VK_IMAGE_USAGE_TRANSIENT_ATTACHMENT_BIT,
                    VMA_MEMORY_USAGE_AUTO_PREFER_DEVICE,
                    1,
                    pColorImage,
                    pColorImageAllocation
            );
            long image = pColorImage.get(0);
            long imageAllocation = pColorImageAllocation.get(0);

            long imageView = application.getImageManager().createImageView(image, imageFormat, VK_IMAGE_ASPECT_COLOR_BIT, 1);

            // Will later be true because of the specified attachment transition after renderpass
            int finalLayout = VK_IMAGE_LAYOUT_COLOR_ATTACHMENT_OPTIMAL;
            texture.set(image, imageAllocation, imageView, finalLayout);
        }
    }
}
