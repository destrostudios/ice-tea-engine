package com.destrostudios.icetea.core;

import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.*;

import java.nio.LongBuffer;

import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.vulkan.VK10.*;

public class ImageManager {

    public ImageManager(Application application) {
        this.application = application;
    }
    private Application application;

    public void createImage(
        int width,
        int height,
        int mipLevels,
        int numSamples,
        int format,
        int usage,
        int memProperties,
        int arrayLayers,
        LongBuffer pTextureImage,
        LongBuffer pTextureImageMemory
    ) {
        try (MemoryStack stack = stackPush()) {
            VkImageCreateInfo imageCreateInfo = VkImageCreateInfo.callocStack(stack);
            imageCreateInfo.sType(VK_STRUCTURE_TYPE_IMAGE_CREATE_INFO);
            imageCreateInfo.imageType(VK_IMAGE_TYPE_2D);
            imageCreateInfo.extent().width(width);
            imageCreateInfo.extent().height(height);
            imageCreateInfo.extent().depth(1);
            imageCreateInfo.mipLevels(mipLevels);
            imageCreateInfo.arrayLayers(arrayLayers);
            imageCreateInfo.format(format);
            imageCreateInfo.tiling(VK_IMAGE_TILING_OPTIMAL);
            imageCreateInfo.initialLayout(VK_IMAGE_LAYOUT_UNDEFINED);
            imageCreateInfo.usage(usage);
            imageCreateInfo.samples(numSamples);
            imageCreateInfo.sharingMode(VK_SHARING_MODE_EXCLUSIVE);

            int result = vkCreateImage(application.getLogicalDevice(), imageCreateInfo, null, pTextureImage);
            if (result != VK_SUCCESS) {
                throw new RuntimeException("Failed to create image (result = " + result + ")");
            }

            VkMemoryAllocateInfo allocateInfo = VkMemoryAllocateInfo.callocStack(stack);
            allocateInfo.sType(VK_STRUCTURE_TYPE_MEMORY_ALLOCATE_INFO);
            VkMemoryRequirements memoryRequirements = VkMemoryRequirements.mallocStack(stack);
            vkGetImageMemoryRequirements(application.getLogicalDevice(), pTextureImage.get(0), memoryRequirements);
            allocateInfo.allocationSize(memoryRequirements.size());
            allocateInfo.memoryTypeIndex(application.findMemoryType(memoryRequirements.memoryTypeBits(), memProperties));

            result = vkAllocateMemory(application.getLogicalDevice(), allocateInfo, null, pTextureImageMemory);
            if (result != VK_SUCCESS) {
                throw new RuntimeException("Failed to allocate image memory (result = " + result + ")");
            }
            vkBindImageMemory(application.getLogicalDevice(), pTextureImage.get(0), pTextureImageMemory.get(0), 0);
        }
    }

    public long createImageView(long image, int format, int aspectFlags, int mipLevels) {
        return createImageView(image, format, aspectFlags, mipLevels, VK_IMAGE_VIEW_TYPE_2D, 1, 0);
    }

    public long createImageView(long image, int format, int aspectFlags, int mipLevels, int viewType, int layerCount, int baseArrayLayer) {
        try (MemoryStack stack = stackPush()) {
            VkImageViewCreateInfo viewInfo = VkImageViewCreateInfo.callocStack(stack);
            viewInfo.sType(VK_STRUCTURE_TYPE_IMAGE_VIEW_CREATE_INFO);
            viewInfo.image(image);
            viewInfo.viewType(viewType);
            viewInfo.format(format);
            viewInfo.subresourceRange().aspectMask(aspectFlags);
            viewInfo.subresourceRange().baseMipLevel(0);
            viewInfo.subresourceRange().levelCount(mipLevels);
            viewInfo.subresourceRange().baseArrayLayer(baseArrayLayer);
            viewInfo.subresourceRange().layerCount(layerCount);

            LongBuffer pImageView = stack.mallocLong(1);
            int result = vkCreateImageView(application.getLogicalDevice(), viewInfo, null, pImageView);
            if (result != VK_SUCCESS) {
                throw new RuntimeException("Failed to create texture image view (result = " + result + ")");
            }
            return pImageView.get(0);
        }
    }

    public void transitionImageLayout(long image, int format, int oldLayout, int newLayout, int mipLevels) {
        try (MemoryStack stack = stackPush()) {
            VkImageMemoryBarrier.Buffer barrier = VkImageMemoryBarrier.callocStack(1, stack);
            barrier.sType(VK_STRUCTURE_TYPE_IMAGE_MEMORY_BARRIER);
            barrier.oldLayout(oldLayout);
            barrier.newLayout(newLayout);
            barrier.srcQueueFamilyIndex(VK_QUEUE_FAMILY_IGNORED);
            barrier.dstQueueFamilyIndex(VK_QUEUE_FAMILY_IGNORED);
            barrier.image(image);

            barrier.subresourceRange().baseMipLevel(0);
            barrier.subresourceRange().levelCount(mipLevels);
            barrier.subresourceRange().baseArrayLayer(0);
            barrier.subresourceRange().layerCount(1);

            if (newLayout == VK_IMAGE_LAYOUT_DEPTH_STENCIL_ATTACHMENT_OPTIMAL) {
                barrier.subresourceRange().aspectMask(VK_IMAGE_ASPECT_DEPTH_BIT);
                if (hasStencilComponent(format)) {
                    barrier.subresourceRange().aspectMask(barrier.subresourceRange().aspectMask() | VK_IMAGE_ASPECT_STENCIL_BIT);
                }
            } else {
                barrier.subresourceRange().aspectMask(VK_IMAGE_ASPECT_COLOR_BIT);
            }

            int sourceStage;
            int destinationStage;

            if ((oldLayout == VK_IMAGE_LAYOUT_UNDEFINED) && (newLayout == VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL)) {
                barrier.srcAccessMask(0);
                barrier.dstAccessMask(VK_ACCESS_TRANSFER_WRITE_BIT);
                sourceStage = VK_PIPELINE_STAGE_TOP_OF_PIPE_BIT;
                destinationStage = VK_PIPELINE_STAGE_TRANSFER_BIT;
            } else if ((oldLayout == VK_IMAGE_LAYOUT_GENERAL) && (newLayout == VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL)) {
                barrier.srcAccessMask(0);
                barrier.dstAccessMask(VK_ACCESS_TRANSFER_WRITE_BIT);
                sourceStage = VK_PIPELINE_STAGE_TOP_OF_PIPE_BIT;
                destinationStage = VK_PIPELINE_STAGE_TRANSFER_BIT;
            } else if ((oldLayout == VK_IMAGE_LAYOUT_UNDEFINED) && (newLayout == VK_IMAGE_LAYOUT_GENERAL)) {
                barrier.srcAccessMask(0);
                barrier.dstAccessMask(VK_ACCESS_SHADER_READ_BIT | VK_ACCESS_SHADER_WRITE_BIT);
                sourceStage = VK_PIPELINE_STAGE_TOP_OF_PIPE_BIT;
                destinationStage = VK_PIPELINE_STAGE_ALL_COMMANDS_BIT; // TODO: Specify more exactly, but will be refactored anyways
            } else if((oldLayout == VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL) && (newLayout == VK_IMAGE_LAYOUT_SHADER_READ_ONLY_OPTIMAL)) {
                barrier.srcAccessMask(VK_ACCESS_TRANSFER_WRITE_BIT);
                barrier.dstAccessMask(VK_ACCESS_SHADER_READ_BIT);
                sourceStage = VK_PIPELINE_STAGE_TRANSFER_BIT;
                destinationStage = VK_PIPELINE_STAGE_FRAGMENT_SHADER_BIT;
            } else if ((oldLayout == VK_IMAGE_LAYOUT_UNDEFINED) && (newLayout == VK_IMAGE_LAYOUT_DEPTH_STENCIL_ATTACHMENT_OPTIMAL)) {
                barrier.srcAccessMask(0);
                barrier.dstAccessMask(VK_ACCESS_DEPTH_STENCIL_ATTACHMENT_READ_BIT | VK_ACCESS_DEPTH_STENCIL_ATTACHMENT_WRITE_BIT);
                sourceStage = VK_PIPELINE_STAGE_TOP_OF_PIPE_BIT;
                destinationStage = VK_PIPELINE_STAGE_EARLY_FRAGMENT_TESTS_BIT;
            } else if ((oldLayout == VK_IMAGE_LAYOUT_UNDEFINED) && (newLayout == VK_IMAGE_LAYOUT_COLOR_ATTACHMENT_OPTIMAL)) {
                barrier.srcAccessMask(0);
                barrier.dstAccessMask(VK_ACCESS_COLOR_ATTACHMENT_READ_BIT | VK_ACCESS_COLOR_ATTACHMENT_WRITE_BIT);
                sourceStage = VK_PIPELINE_STAGE_TOP_OF_PIPE_BIT;
                destinationStage = VK_PIPELINE_STAGE_COLOR_ATTACHMENT_OUTPUT_BIT;
            } else {
                throw new IllegalArgumentException("Unsupported layout transition");
            }

            VkCommandBuffer commandBuffer = application.getCommandPool().beginSingleTimeCommands();
            vkCmdPipelineBarrier(
                commandBuffer,
                sourceStage,
                destinationStage,
                0,
                null,
                null,
                barrier
            );
            application.getCommandPool().endSingleTimeCommands(commandBuffer);
        }
    }

    private boolean hasStencilComponent(int format) {
        return ((format == VK_FORMAT_D32_SFLOAT_S8_UINT) || (format == VK_FORMAT_D24_UNORM_S8_UINT));
    }

    public void copyBufferToImage(long buffer, long image, int width, int height) {
        try (MemoryStack stack = stackPush()) {
            VkBufferImageCopy.Buffer region = VkBufferImageCopy.callocStack(1, stack);
            region.bufferOffset(0);
            region.bufferRowLength(0); // Tightly packed
            region.bufferImageHeight(0); // Tightly packed
            region.imageSubresource().aspectMask(VK_IMAGE_ASPECT_COLOR_BIT);
            region.imageSubresource().mipLevel(0);
            region.imageSubresource().baseArrayLayer(0);
            region.imageSubresource().layerCount(1);
            region.imageOffset().set(0, 0, 0);
            region.imageExtent(VkExtent3D.callocStack(stack).set(width, height, 1));

            VkCommandBuffer commandBuffer = application.getCommandPool().beginSingleTimeCommands();
            vkCmdCopyBufferToImage(commandBuffer, buffer, image, VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL, region);
            application.getCommandPool().endSingleTimeCommands(commandBuffer);
        }
    }

    public void generateMipmaps(long image, int imageFormat, int width, int height, int mipLevels, int oldLayout, int finalLayout, int finalDstAccessMask, int finalDstStageMask) {
        try (MemoryStack stack = stackPush()) {
            // Check if image format supports linear blitting
            VkFormatProperties formatProperties = VkFormatProperties.mallocStack(stack);
            vkGetPhysicalDeviceFormatProperties(application.getPhysicalDevice(), imageFormat, formatProperties);

            if ((formatProperties.optimalTilingFeatures() & VK_FORMAT_FEATURE_SAMPLED_IMAGE_FILTER_LINEAR_BIT) == 0) {
                throw new RuntimeException("Texture image format does not support linear blitting");
            }

            VkCommandBuffer commandBuffer = application.getCommandPool().beginSingleTimeCommands();

            int mipWidth = width;
            int mipHeight = height;

            VkImageMemoryBarrier.Buffer barrier = VkImageMemoryBarrier.callocStack(1, stack);
            barrier.sType(VK_STRUCTURE_TYPE_IMAGE_MEMORY_BARRIER);
            barrier.image(image);
            barrier.srcQueueFamilyIndex(VK_QUEUE_FAMILY_IGNORED);
            barrier.dstQueueFamilyIndex(VK_QUEUE_FAMILY_IGNORED);
            barrier.dstAccessMask(VK_QUEUE_FAMILY_IGNORED);
            barrier.subresourceRange().aspectMask(VK_IMAGE_ASPECT_COLOR_BIT);
            barrier.subresourceRange().baseArrayLayer(0);
            barrier.subresourceRange().layerCount(1);
            barrier.subresourceRange().levelCount(1);
            for (int i = 1; i < mipLevels; i++) {
                barrier.subresourceRange().baseMipLevel(i - 1);
                barrier.oldLayout(oldLayout);
                barrier.newLayout(VK_IMAGE_LAYOUT_TRANSFER_SRC_OPTIMAL);
                barrier.srcAccessMask(VK_ACCESS_TRANSFER_WRITE_BIT);
                barrier.dstAccessMask(VK_ACCESS_TRANSFER_READ_BIT);

                vkCmdPipelineBarrier(
                        commandBuffer,
                        VK_PIPELINE_STAGE_TRANSFER_BIT,
                        VK_PIPELINE_STAGE_TRANSFER_BIT,
                        0,
                        null,
                        null,
                        barrier
                );

                VkImageBlit.Buffer blit = VkImageBlit.callocStack(1, stack);
                blit.srcOffsets(0).set(0, 0, 0);
                blit.srcOffsets(1).set(mipWidth, mipHeight, 1);
                blit.srcSubresource().aspectMask(VK_IMAGE_ASPECT_COLOR_BIT);
                blit.srcSubresource().mipLevel(i - 1);
                blit.srcSubresource().baseArrayLayer(0);
                blit.srcSubresource().layerCount(1);
                blit.dstOffsets(0).set(0, 0, 0);
                blit.dstOffsets(1).set(mipWidth > 1 ? mipWidth / 2 : 1, mipHeight > 1 ? mipHeight / 2 : 1, 1);
                blit.dstSubresource().aspectMask(VK_IMAGE_ASPECT_COLOR_BIT);
                blit.dstSubresource().mipLevel(i);
                blit.dstSubresource().baseArrayLayer(0);
                blit.dstSubresource().layerCount(1);

                vkCmdBlitImage(
                    commandBuffer,
                    image,
                    VK_IMAGE_LAYOUT_TRANSFER_SRC_OPTIMAL,
                    image,
                    oldLayout,
                    blit,
                    VK_FILTER_LINEAR
                );

                barrier.oldLayout(VK_IMAGE_LAYOUT_TRANSFER_SRC_OPTIMAL);
                barrier.newLayout(finalLayout);
                barrier.srcAccessMask(VK_ACCESS_TRANSFER_READ_BIT);
                barrier.dstAccessMask(VK_ACCESS_SHADER_READ_BIT);

                vkCmdPipelineBarrier(
                    commandBuffer,
                    VK_PIPELINE_STAGE_TRANSFER_BIT,
                    VK_PIPELINE_STAGE_FRAGMENT_SHADER_BIT,
                    0,
                    null,
                    null,
                    barrier
                );

                if (mipWidth > 1) {
                    mipWidth /= 2;
                }
                if (mipHeight > 1) {
                    mipHeight /= 2;
                }
            }
            barrier.subresourceRange().baseMipLevel(mipLevels - 1);
            barrier.oldLayout(oldLayout);
            barrier.newLayout(finalLayout);
            barrier.srcAccessMask(VK_ACCESS_TRANSFER_WRITE_BIT);
            barrier.dstAccessMask(finalDstAccessMask);

            vkCmdPipelineBarrier(
                commandBuffer,
                VK_PIPELINE_STAGE_TRANSFER_BIT,
                finalDstStageMask,
                0,
                null,
                null,
                barrier
            );
            application.getCommandPool().endSingleTimeCommands(commandBuffer);
        }
    }
}
