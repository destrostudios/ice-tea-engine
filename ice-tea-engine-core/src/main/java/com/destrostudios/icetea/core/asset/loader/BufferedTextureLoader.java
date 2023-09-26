package com.destrostudios.icetea.core.asset.loader;

import com.destrostudios.icetea.core.asset.AssetLoader;
import com.destrostudios.icetea.core.resource.descriptor.SimpleTextureDescriptor;
import com.destrostudios.icetea.core.texture.BufferedTexture;
import com.destrostudios.icetea.core.texture.TextureData;
import org.lwjgl.BufferUtils;
import org.lwjgl.system.MemoryStack;

import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import static org.lwjgl.stb.STBImage.*;
import static org.lwjgl.system.MemoryStack.stackPush;

public class BufferedTextureLoader extends AssetLoader<BufferedTexture, BufferedTextureLoaderSettings> {

    @Override
    public BufferedTexture load() {
        try (MemoryStack stack = stackPush()) {
            BufferedTexture bufferedTexture = new BufferedTexture(() -> {
                try (InputStream inputStream = assetKey.openInputStream()) {
                    byte[] imageData = inputStream.readAllBytes();
                    ByteBuffer imageBuffer = BufferUtils.createByteBuffer(imageData.length);
                    imageBuffer.put(imageData);
                    imageBuffer.flip();

                    // While STB isn't guaranteed to return sRGB values for very old formats, we don't expect such cases and choose it for its lightweight and presumably speed over (sRGB guaranteed) ImageIO.read
                    IntBuffer pWidth = stack.mallocInt(1);
                    IntBuffer pHeight = stack.mallocInt(1);
                    IntBuffer pChannels = stack.mallocInt(1);
                    ByteBuffer pixels = stbi_load_from_memory(imageBuffer, pWidth, pHeight, pChannels, STBI_rgb_alpha);
                    return new TextureData(pixels, pWidth.get(0), pHeight.get(0), () -> stbi_image_free(pixels));
                }
            }, settings.getFormat(), settings.getUsage(), settings.getLayout());
            if (settings.isCreateDefaultDescriptor()) {
                bufferedTexture.setDescriptor("default", new SimpleTextureDescriptor());
            }
            return bufferedTexture;
        }
    }
}
