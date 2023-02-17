package com.destrostudios.icetea.core.buffer;

import com.destrostudios.icetea.core.clone.CloneContext;
import com.destrostudios.icetea.core.clone.ContextCloneable;
import com.destrostudios.icetea.core.lifecycle.LifecycleObject;

import java.nio.ByteBuffer;
import java.util.function.Consumer;

public abstract class ResizableBuffer extends LifecycleObject implements ContextCloneable {

    public ResizableBuffer() { }

    public ResizableBuffer(ResizableBuffer resizableBuffer) {
        this.size = resizableBuffer.size;
    }
    protected long size;

    public boolean write(long size, Consumer<ByteBuffer> write) {
        boolean resized = setSizeAndRecreateIfNecessary(size);
        if (size != 0) {
            write(write);
        }
        return resized;
    }

    protected boolean setSizeAndRecreateIfNecessary(long size) {
        if (size != this.size) {
            this.size = size;
            recreate();
            return true;
        }
        return false;
    }

    private void recreate() {
        cleanupBuffer();
        if (size != 0) {
            createBuffer();
        }
    }

    protected abstract void createBuffer();

    protected abstract void write(Consumer<ByteBuffer> write);

    @Override
    protected void cleanupInternal() {
        cleanupBuffer();
        size = 0;
        super.cleanupInternal();
    }

    protected abstract void cleanupBuffer();

    @Override
    public abstract ResizableBuffer clone(CloneContext context);
}
