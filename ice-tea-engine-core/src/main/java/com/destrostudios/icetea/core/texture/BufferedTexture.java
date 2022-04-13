package com.destrostudios.icetea.core.texture;

import com.destrostudios.icetea.core.Application;
import com.destrostudios.icetea.core.util.BufferUtil;
import com.destrostudios.icetea.core.util.MathUtil;
import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.*;

import java.nio.ByteBuffer;
import java.nio.LongBuffer;

import static org.lwjgl.stb.STBImage.*;
import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.vulkan.VK10.*;

public class BufferedTexture extends Texture {

    public BufferedTexture(ByteBuffer pixels, int width, int height, int channels) {
        this.pixels = pixels;
        this.width = width;
        this.height = height;
        this.channels = channels;
    }
    private ByteBuffer pixels;
    private int width;
    private int height;
    private int channels;
    private int mipLevels;

    @Override
    public void init(Application application) {
        super.init(application);
        int format = VK_FORMAT_R8G8B8A8_SRGB;
        initImage(format);
        initImageView(format);
        initImageSampler();
    }

    private void initImage(int format) {
        try (MemoryStack stack = stackPush()) {
            long imageSize = width * height * 4; // channels
            mipLevels = (int) (Math.floor(MathUtil.log2(Math.max(width, height))) + 1);

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
                width,
                height,
                mipLevels,
                VK_SAMPLE_COUNT_1_BIT,
                format,
                VK_IMAGE_TILING_OPTIMAL,
                VK_IMAGE_USAGE_TRANSFER_SRC_BIT | VK_IMAGE_USAGE_TRANSFER_DST_BIT | VK_IMAGE_USAGE_SAMPLED_BIT | VK_IMAGE_USAGE_STORAGE_BIT,
                VK_MEMORY_PROPERTY_DEVICE_LOCAL_BIT,
                pTextureImage,
                pTextureImageMemory
            );

            image = pTextureImage.get(0);
            imageMemory = pTextureImageMemory.get(0);

            application.getImageManager().transitionImageLayout(
                image,
                format,
                VK_IMAGE_LAYOUT_UNDEFINED,
                VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL,
                mipLevels
            );

            application.getImageManager().copyBufferToImage(pStagingBuffer.get(0), image, width, height);

            int finalLayout = VK_IMAGE_LAYOUT_SHADER_READ_ONLY_OPTIMAL;
            application.getImageManager().generateMipmaps(
                image,
                format,
                width,
                height,
                mipLevels,
                finalLayout,
                VK_ACCESS_SHADER_READ_BIT,
                VK_PIPELINE_STAGE_FRAGMENT_SHADER_BIT
            );
            imageViewLayout = finalLayout;

            vkDestroyBuffer(application.getLogicalDevice(), pStagingBuffer.get(0), null);
            vkFreeMemory(application.getLogicalDevice(), pStagingBufferMemory.get(0), null);
        }
    }

    private void initImageView(int format) {
        imageView = application.getImageManager().createImageView(image, format, VK_IMAGE_ASPECT_COLOR_BIT, mipLevels);
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
            int result = vkCreateSampler(application.getLogicalDevice(), samplerCreateInfo, null, pImageSampler);
            if (result != VK_SUCCESS) {
                throw new RuntimeException("Failed to create texture sampler (result = " + result + ")");
            }
            imageSampler = pImageSampler.get(0);
        }
    }
}
