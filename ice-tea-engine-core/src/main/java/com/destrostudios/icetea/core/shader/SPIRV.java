package com.destrostudios.icetea.core.shader;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.lwjgl.system.NativeResource;

import java.nio.ByteBuffer;

import static org.lwjgl.util.shaderc.Shaderc.shaderc_result_release;

@AllArgsConstructor
public final class SPIRV implements NativeResource {

    private long handle;
    @Getter
    private ByteBuffer byteCode;

    @Override
    public void free() {
        shaderc_result_release(handle);
        byteCode = null; // Help the GC
    }
}