package com.destrostudios.icetea.core.texture;

import com.destrostudios.icetea.core.buffer.StagingResizableMemoryBuffer;
import com.destrostudios.icetea.core.util.BufferUtil;
import com.destrostudios.icetea.core.util.ImageUtil;
import org.lwjgl.system.MemoryStack;

import java.io.IOException;

import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.vulkan.VK10.*;

public class BufferedTexture extends Texture {

    public BufferedTexture(boolean generateMipMaps, int finalLayout, TextureDataReader dataReader) {
        this.generateMipMaps = generateMipMaps;
        this.finalLayout = finalLayout;
        this.dataReader = dataReader;
    }
    private boolean generateMipMaps;
    private int finalLayout;
    private TextureDataReader dataReader;

    @Override
    protected void initNative() {
        super.initNative();
        try (MemoryStack stack = stackPush()) {
            initImage(stack);
            createImageView(stack);
            createSampler(stack);
        }
    }

    private void initImage(MemoryStack stack) {
        TextureData textureData;
        try {
            textureData = dataReader.read();
        } catch (IOException ex) {
            throw new RuntimeException("Failed to read texture (exception = \"" + ex.getMessage() + "\")");
        }
        width = textureData.getWidth();
        height = textureData.getHeight();

        long imageBytes = ((long) textureData.getWidth()) * textureData.getHeight() * ImageUtil.getBytesPerPixel(format);
        StagingResizableMemoryBuffer stagingBuffer = new StagingResizableMemoryBuffer();
        stagingBuffer.updateNative(application);
        stagingBuffer.write(imageBytes, byteBuffer -> {
            BufferUtil.memcpy(textureData.getPixels(), byteBuffer, imageBytes);
        });

        // Texture data is cleaned up from RAM immediately, will be read again if texture is cleanuped and reinitialized
        textureData.getCleanup().run();

        // Required for copying from buffer
        usage |= VK_IMAGE_USAGE_TRANSFER_DST_BIT;
        if (generateMipMaps) {
            mipLevels = ImageUtil.getRecommendedMipLevels(Math.max(textureData.getWidth(), textureData.getHeight()));
            // Required for mipmap generation
            usage |= VK_IMAGE_USAGE_TRANSFER_SRC_BIT | VK_IMAGE_USAGE_TRANSFER_DST_BIT;
        }

        createImage(stack);

        application.getCommandPool().executeSingleTimeCommands(commandBuffer -> {
            transitionLayout(
                commandBuffer,
                finalLayout,
                VK_PIPELINE_STAGE_ALL_COMMANDS_BIT,
                0,
                VK_PIPELINE_STAGE_ALL_COMMANDS_BIT,
                0,
                stack
            );

            copyFromBuffer(commandBuffer, stagingBuffer.getBuffer(), stack);

            if (generateMipMaps) {
                generateMipmaps(commandBuffer, stack);
            }
        });
        stagingBuffer.cleanupNative();
    }
}
