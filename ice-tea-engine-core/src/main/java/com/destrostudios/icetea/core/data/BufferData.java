package com.destrostudios.icetea.core.data;

import com.destrostudios.icetea.core.clone.CloneContext;
import com.destrostudios.icetea.core.data.values.UniformValue;
import lombok.Getter;

import java.nio.ByteBuffer;

public abstract class BufferData extends FieldsData {

    public BufferData() { }

    public BufferData(BufferData bufferData, CloneContext context) {
        super(bufferData, context);
    }
    @Getter
    private boolean wasBufferRecreated;

    @Override
    protected void update(float tpf) {
        super.update(tpf);
        wasBufferRecreated = recreateBufferIfNecessary();
        updateBufferIfNecessary();
    }

    private boolean recreateBufferIfNecessary() {
        if (structureModified) {
            cleanupBuffer();
            initBuffer();
            structureModified = false;
            return true;
        }
        return false;
    }

    private void initBuffer() {
        if (size > 0) {
            initBufferInternal();
            contentModified = true;
        }
        structureModified = false;
    }

    protected abstract void initBufferInternal();

    private void updateBufferIfNecessary() {
        if ((size > 0) && contentModified) {
            ByteBuffer byteBuffer = prepareUpdatingBuffer();
            int index = 0;
            for (UniformValue<?> value : fields.values()) {
                value.write(byteBuffer, index);
                index += value.getAlignedSize();
            }
            finishUpdatingBuffer();
            contentModified = false;
        }
    }

    protected abstract ByteBuffer prepareUpdatingBuffer();

    protected abstract void finishUpdatingBuffer();

    @Override
    protected int getSize(UniformValue<?> uniformValue) {
        return uniformValue.getAlignedSize();
    }

    @Override
    protected void cleanupInternal() {
        cleanupBuffer();
        structureModified = true;
        super.cleanupInternal();
    }

    protected abstract void cleanupBuffer();

    @Override
    public abstract BufferData clone(CloneContext context);
}