package com.destrostudios.icetea.core.texture;

import com.destrostudios.icetea.core.buffer.StagingResizableMemoryBuffer;
import com.destrostudios.icetea.core.util.BufferUtil;
import com.destrostudios.icetea.core.util.MathUtil;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.*;

import java.io.IOException;
import java.nio.LongBuffer;

import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.vulkan.VK10.*;

public class BufferedTexture extends Texture {

    public BufferedTexture(TextureDataReader dataReader, int format, int usage, int layout) {
        this.dataReader = dataReader;
        this.format = format;
        this.usage = usage;
        this.layout = layout;
    }
    private TextureDataReader dataReader;
    private int format;
    private int usage;
    private int layout;
    private int mipLevels;

    @Override
    protected void init() {
        super.init();
        initImage();
        initImageView();
        initImageSampler();
        onSet();
    }

    private void initImage() {
        try (MemoryStack stack = stackPush()) {
            TextureData textureData;
            try {
                textureData = dataReader.read();
            } catch (IOException ex) {
                throw new RuntimeException("Failed to read texture (exception = \"" + ex.getMessage() + "\")");
            }
            long imageBytes = ((long) textureData.getWidth()) * textureData.getHeight() * 4; // channels

            StagingResizableMemoryBuffer stagingBuffer = new StagingResizableMemoryBuffer();
            stagingBuffer.update(application, 0);
            stagingBuffer.write(imageBytes, byteBuffer -> {
                BufferUtil.memcpy(textureData.getPixels(), byteBuffer, imageBytes);
            });

            // Texture data is cleaned up from RAM immediately, will be read again if texture is cleanuped and reinitialized
            textureData.getCleanup().run();

            mipLevels = (int) (Math.floor(MathUtil.log2(Math.max(textureData.getWidth(), textureData.getHeight()))) + 1);
            LongBuffer pTextureImage = stack.mallocLong(1);
            LongBuffer pTextureImageMemory = stack.mallocLong(1);
            application.getImageManager().createImage(
                textureData.getWidth(),
                textureData.getHeight(),
                mipLevels,
                VK_SAMPLE_COUNT_1_BIT,
                format,
                // TRANSFER_SRC and TRANSFER_DST are needed for the mipmap generation below
                VK_IMAGE_USAGE_TRANSFER_SRC_BIT | VK_IMAGE_USAGE_TRANSFER_DST_BIT | usage,
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

            application.getImageManager().copyBufferToImage(stagingBuffer.getBuffer(), image, textureData.getWidth(), textureData.getHeight());
            stagingBuffer.cleanup();

            application.getImageManager().generateMipmaps(
                image,
                format,
                textureData.getWidth(),
                textureData.getHeight(),
                mipLevels,
                VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL,
                layout,
                VK_ACCESS_SHADER_READ_BIT,
                VK_PIPELINE_STAGE_FRAGMENT_SHADER_BIT
            );
            imageViewLayout = layout;
        }
    }

    private void initImageView() {
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
            samplerCreateInfo.maxAnisotropy(16);
            samplerCreateInfo.borderColor(VK_BORDER_COLOR_INT_OPAQUE_BLACK);
            samplerCreateInfo.unnormalizedCoordinates(false);
            samplerCreateInfo.compareEnable(false);
            samplerCreateInfo.compareOp(VK_COMPARE_OP_ALWAYS);
            samplerCreateInfo.mipmapMode(VK_SAMPLER_MIPMAP_MODE_LINEAR);
            samplerCreateInfo.minLod(0); // Optional
            samplerCreateInfo.maxLod(mipLevels);
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
