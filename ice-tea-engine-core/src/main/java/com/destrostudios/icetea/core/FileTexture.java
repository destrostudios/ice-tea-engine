package com.destrostudios.icetea.core;

import org.lwjgl.BufferUtils;
import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.*;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.LongBuffer;

import static java.lang.ClassLoader.getSystemClassLoader;
import static org.lwjgl.stb.STBImage.*;
import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.vulkan.VK10.*;

public class FileTexture extends Texture {

    public FileTexture(String filePath) {
        this.filePath = filePath;
    }
    private String filePath;
    private int mipLevels;

    @Override
    public void init(Application application) {
        super.init(application);
        initImage();
        initImageView();
        initImageSampler();
    }

    private void initImage() {
        try (MemoryStack stack = stackPush()) {
            IntBuffer pWidth = stack.mallocInt(1);
            IntBuffer pHeight = stack.mallocInt(1);
            IntBuffer pChannels = stack.mallocInt(1);

            ByteBuffer pixels = null;
            try {
                InputStream inputStream = getSystemClassLoader().getResourceAsStream(filePath);
                byte[] imageData = inputStream.readAllBytes();
                ByteBuffer imageBuffer = BufferUtils.createByteBuffer(imageData.length);
                imageBuffer.put(imageData);
                imageBuffer.flip();
                pixels = stbi_load_from_memory(imageBuffer, pWidth, pHeight, pChannels, STBI_rgb_alpha);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            if (pixels == null) {
                throw new RuntimeException("Failed to load texture '" + filePath + "'");
            }

            long imageSize = pWidth.get(0) * pHeight.get(0) * 4; // pChannels.get(0);

            mipLevels = (int) (Math.floor(MathUtil.log2(Math.max(pWidth.get(0), pHeight.get(0)))) + 1);

            LongBuffer pStagingBuffer = stack.mallocLong(1);
            LongBuffer pStagingBufferMemory = stack.mallocLong(1);
            application.getBufferManager().createBuffer(
                imageSize,
                VK_BUFFER_USAGE_TRANSFER_SRC_BIT,
                VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT | VK_MEMORY_PROPERTY_HOST_COHERENT_BIT,
                pStagingBuffer,
                pStagingBufferMemory
            );

            PointerBuffer data = stack.mallocPointer(1);
            vkMapMemory(application.getLogicalDevice(), pStagingBufferMemory.get(0), 0, imageSize, 0, data);
            BufferUtil.memcpy(pixels, data.getByteBuffer(0, (int) imageSize), imageSize);
            vkUnmapMemory(application.getLogicalDevice(), pStagingBufferMemory.get(0));

            stbi_image_free(pixels);

            LongBuffer pTextureImage = stack.mallocLong(1);
            LongBuffer pTextureImageMemory = stack.mallocLong(1);
            application.getImageManager().createImage(
                pWidth.get(0),
                pHeight.get(0),
                mipLevels,
                VK_SAMPLE_COUNT_1_BIT,
                VK_FORMAT_R8G8B8A8_SRGB,
                VK_IMAGE_TILING_OPTIMAL,
                VK_IMAGE_USAGE_TRANSFER_SRC_BIT | VK_IMAGE_USAGE_TRANSFER_DST_BIT | VK_IMAGE_USAGE_SAMPLED_BIT,
                VK_MEMORY_PROPERTY_DEVICE_LOCAL_BIT,
                pTextureImage,
                pTextureImageMemory
            );

            image = pTextureImage.get(0);
            imageMemory = pTextureImageMemory.get(0);

            application.getImageManager().transitionImageLayout(
                image,
                VK_FORMAT_R8G8B8A8_SRGB,
                VK_IMAGE_LAYOUT_UNDEFINED,
                VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL,
                mipLevels
            );

            application.getImageManager().copyBufferToImage(pStagingBuffer.get(0), image, pWidth.get(0), pHeight.get(0));

            // Transitioned to VK_IMAGE_LAYOUT_SHADER_READ_ONLY_OPTIMAL while generating mipmaps
            generateMipmaps(image, VK_FORMAT_R8G8B8A8_SRGB, pWidth.get(0), pHeight.get(0), mipLevels);

            vkDestroyBuffer(application.getLogicalDevice(), pStagingBuffer.get(0), null);
            vkFreeMemory(application.getLogicalDevice(), pStagingBufferMemory.get(0), null);
        }
    }

    private void generateMipmaps(long image, int imageFormat, int width, int height, int mipLevels) {
        try (MemoryStack stack = stackPush()) {
            // Check if image format supports linear blitting
            VkFormatProperties formatProperties = VkFormatProperties.mallocStack(stack);
            vkGetPhysicalDeviceFormatProperties(application.getPhysicalDevice(), imageFormat, formatProperties);

            if ((formatProperties.optimalTilingFeatures() & VK_FORMAT_FEATURE_SAMPLED_IMAGE_FILTER_LINEAR_BIT) == 0) {
                throw new RuntimeException("Texture image format does not support linear blitting");
            }

            VkCommandBuffer commandBuffer = application.getBufferManager().beginSingleTimeCommands();

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
                barrier.oldLayout(VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL);
                barrier.newLayout(VK_IMAGE_LAYOUT_TRANSFER_SRC_OPTIMAL);
                barrier.srcAccessMask(VK_ACCESS_TRANSFER_WRITE_BIT);
                barrier.dstAccessMask(VK_ACCESS_TRANSFER_READ_BIT);

                vkCmdPipelineBarrier(
                    commandBuffer,
                    VK_PIPELINE_STAGE_TRANSFER_BIT, VK_PIPELINE_STAGE_TRANSFER_BIT, 0,
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
                    VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL,
                    blit,
                    VK_FILTER_LINEAR
                );

                barrier.oldLayout(VK_IMAGE_LAYOUT_TRANSFER_SRC_OPTIMAL);
                barrier.newLayout(VK_IMAGE_LAYOUT_SHADER_READ_ONLY_OPTIMAL);
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
            barrier.oldLayout(VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL);
            barrier.newLayout(VK_IMAGE_LAYOUT_SHADER_READ_ONLY_OPTIMAL);
            barrier.srcAccessMask(VK_ACCESS_TRANSFER_WRITE_BIT);
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
            application.getBufferManager().endSingleTimeCommands(commandBuffer);
        }
    }

    private void initImageView() {
        imageView = application.getImageManager().createImageView(image, VK_FORMAT_R8G8B8A8_SRGB, VK_IMAGE_ASPECT_COLOR_BIT, mipLevels);
    }

    private void initImageSampler() {
        try (MemoryStack stack = stackPush()) {
            VkSamplerCreateInfo samplerCreateInfo = VkSamplerCreateInfo.callocStack(stack);
            samplerCreateInfo.sType(VK_STRUCTURE_TYPE_SAMPLER_CREATE_INFO);
            samplerCreateInfo.magFilter(VK_FILTER_LINEAR);
            samplerCreateInfo.minFilter(VK_FILTER_LINEAR);
            samplerCreateInfo.addressModeU(VK_SAMPLER_ADDRESS_MODE_REPEAT);
            samplerCreateInfo.addressModeV(VK_SAMPLER_ADDRESS_MODE_REPEAT);
            samplerCreateInfo.addressModeW(VK_SAMPLER_ADDRESS_MODE_REPEAT);
            samplerCreateInfo.anisotropyEnable(true);
            samplerCreateInfo.maxAnisotropy(16.0f);
            samplerCreateInfo.borderColor(VK_BORDER_COLOR_INT_OPAQUE_BLACK);
            samplerCreateInfo.unnormalizedCoordinates(false);
            samplerCreateInfo.compareEnable(false);
            samplerCreateInfo.compareOp(VK_COMPARE_OP_ALWAYS);
            samplerCreateInfo.mipmapMode(VK_SAMPLER_MIPMAP_MODE_LINEAR);
            samplerCreateInfo.minLod(0); // Optional
            samplerCreateInfo.maxLod((float) mipLevels);
            samplerCreateInfo.mipLodBias(0); // Optional

            LongBuffer pImageSampler = stack.mallocLong(1);
            if (vkCreateSampler(application.getLogicalDevice(), samplerCreateInfo, null, pImageSampler) != VK_SUCCESS) {
                throw new RuntimeException("Failed to create texture sampler");
            }
            imageSampler = pImageSampler.get(0);
        }
    }
}
