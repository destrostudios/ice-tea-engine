package com.destrostudios.icetea.core.texture;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.nio.ByteBuffer;

@AllArgsConstructor
@Getter
public class TextureData {

    private ByteBuffer pixels;
    private int width;
    private int height;
    private Runnable cleanup;
}
