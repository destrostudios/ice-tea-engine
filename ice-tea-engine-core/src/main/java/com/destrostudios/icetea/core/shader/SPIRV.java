package com.destrostudios.icetea.core.shader;

import org.lwjgl.system.NativeResource;

import java.nio.ByteBuffer;

import static org.lwjgl.util.shaderc.Shaderc.shaderc_result_release;

public final class SPIRV implements NativeResource {

    public SPIRV(long handle, ByteBuffer bytecode) {
        this.handle = handle;
        this.bytecode = bytecode;
    }
    private final long handle;
    private ByteBuffer bytecode;

    public ByteBuffer bytecode() {
        return bytecode;
    }

    @Override
    public void free() {
        shaderc_result_release(handle);
        bytecode = null; // Help the GC
    }
}