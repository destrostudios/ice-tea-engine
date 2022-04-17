package com.destrostudios.icetea.core.texture;

import com.destrostudios.icetea.core.lifecycle.LifecycleObject;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.nio.ByteBuffer;

@AllArgsConstructor
@Getter
public class TextureData extends LifecycleObject {

    private ByteBuffer pixels;
    private int width;
    private int height;
    private int channels;
}
