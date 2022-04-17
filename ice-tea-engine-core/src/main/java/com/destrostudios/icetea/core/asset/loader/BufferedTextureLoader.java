package com.destrostudios.icetea.core.asset.loader;

import com.destrostudios.icetea.core.asset.AssetLoader;
import com.destrostudios.icetea.core.texture.BufferedTexture;
import com.destrostudios.icetea.core.texture.TextureData;
import org.lwjgl.BufferUtils;
import org.lwjgl.system.MemoryStack;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.function.Supplier;

import static org.lwjgl.stb.STBImage.STBI_rgb_alpha;
import static org.lwjgl.stb.STBImage.stbi_load_from_memory;
import static org.lwjgl.system.MemoryStack.stackPush;

public class BufferedTextureLoader extends AssetLoader<BufferedTexture, Void> {

    @Override
    public BufferedTexture load(Supplier<InputStream> inputStreamSupplier) throws IOException {
        try (MemoryStack stack = stackPush()) {
           return new BufferedTexture(() -> {
               try (InputStream inputStream = inputStreamSupplier.get()) {
                   byte[] imageData = inputStream.readAllBytes();
                   ByteBuffer imageBuffer = BufferUtils.createByteBuffer(imageData.length);
                   imageBuffer.put(imageData);
                   imageBuffer.flip();

                   IntBuffer pWidth = stack.mallocInt(1);
                   IntBuffer pHeight = stack.mallocInt(1);
                   IntBuffer pChannels = stack.mallocInt(1);
                   ByteBuffer pixels = stbi_load_from_memory(imageBuffer, pWidth, pHeight, pChannels, STBI_rgb_alpha);
                   return new TextureData(pixels, pWidth.get(0), pHeight.get(0), pChannels.get(0));
               }
           });
        }
    }
}
