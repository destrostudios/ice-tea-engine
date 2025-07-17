package com.destrostudios.icetea.core.asset.loader;

import com.destrostudios.icetea.core.asset.AssetLoader;
import com.destrostudios.icetea.core.resource.descriptor.SimpleTextureDescriptor;
import com.destrostudios.icetea.core.texture.BufferedTexture;
import com.destrostudios.icetea.core.texture.TextureData;
import org.lwjgl.BufferUtils;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;

import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import static org.lwjgl.stb.STBImage.*;
import static org.lwjgl.stb.STBImage.stbi_load_from_memory;
import static org.lwjgl.system.MemoryStack.stackPush;

public class BufferedTextureLoader extends AssetLoader<BufferedTexture, BufferedTextureLoaderSettings> {

    private static final int DESIRED_CHANNELS = STBI_rgb_alpha;

    @Override
    public BufferedTexture load() {
        try (MemoryStack stack = stackPush()) {
            BufferedTexture bufferedTexture = new BufferedTexture(
                settings.isGenerateMipMaps(),
                settings.getLayout(),
                () -> {
                    try (InputStream inputStream = assetKey.openInputStream()) {
                        byte[] imageData = inputStream.readAllBytes();
                        ByteBuffer imageBuffer = BufferUtils.createByteBuffer(imageData.length);
                        imageBuffer.put(imageData);
                        imageBuffer.flip();

                        // STB loads the image data as-is (no automatic sRGB->linear conversion) - Therefore, settings.getFormat() needs to match the image data
                        IntBuffer pWidth = stack.mallocInt(1);
                        IntBuffer pHeight = stack.mallocInt(1);
                        IntBuffer pChannels = stack.mallocInt(1);
                        ByteBuffer pixels;
                        stbi_set_flip_vertically_on_load(settings.isFlipY());
                        if (stbi_is_hdr_from_memory(imageBuffer)) {
                            FloatBuffer floatValues = stbi_loadf_from_memory(imageBuffer, pWidth, pHeight, pChannels, DESIRED_CHANNELS);
                            pixels = MemoryUtil.memByteBuffer(MemoryUtil.memAddress(floatValues), pWidth.get(0) * pHeight.get(0) * DESIRED_CHANNELS * Float.BYTES);
                        } else {
                            pixels = stbi_load_from_memory(imageBuffer, pWidth, pHeight, pChannels, DESIRED_CHANNELS);
                        }
                        return new TextureData(pixels, pWidth.get(0), pHeight.get(0), () -> stbi_image_free(pixels));
                    }
                }
            );
            bufferedTexture.set(
                settings.getAspectMask(),
                settings.getFormat(),
                settings.getUsage()
            );
            if (settings.isCreateDefaultDescriptor()) {
                bufferedTexture.setDescriptor("default", new SimpleTextureDescriptor());
            }
            return bufferedTexture;
        }
    }
}
