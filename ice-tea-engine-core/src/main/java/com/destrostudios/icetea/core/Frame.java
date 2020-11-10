package com.destrostudios.icetea.core;

import lombok.Getter;

import java.nio.LongBuffer;

import static org.lwjgl.system.MemoryStack.stackGet;

@Getter
public class Frame {

    public Frame(long imageAvailableSemaphore, long renderFinishedSemaphore, long fence) {
        this.imageAvailableSemaphore = imageAvailableSemaphore;
        this.renderFinishedSemaphore = renderFinishedSemaphore;
        this.fence = fence;
    }
    private long imageAvailableSemaphore;
    private long renderFinishedSemaphore;
    private long fence;

    public LongBuffer getPImageAvailableSemaphore() {
        return stackGet().longs(imageAvailableSemaphore);
    }

    public LongBuffer getPRenderFinishedSemaphore() {
        return stackGet().longs(renderFinishedSemaphore);
    }

    public LongBuffer getPFence() {
        return stackGet().longs(fence);
    }
}
