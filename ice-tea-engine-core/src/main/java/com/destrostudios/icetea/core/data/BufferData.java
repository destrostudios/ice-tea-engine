package com.destrostudios.icetea.core.data;

import com.destrostudios.icetea.core.Application;
import com.destrostudios.icetea.core.clone.CloneContext;
import com.destrostudios.icetea.core.data.values.UniformValue;

import java.nio.ByteBuffer;
import java.util.ArrayList;

public abstract class BufferData extends FieldsData {

    public BufferData() { }

    public BufferData(BufferData bufferData, CloneContext context) {
        super(bufferData, context);
    }

    public boolean updateBufferAndCheckRecreation(Application application, int bufferIndex, float tpf, int buffersCount) {
        update(application, bufferIndex, tpf);
        boolean wasRecreated = recreateBuffersIfNecessary(buffersCount);
        updateBufferIfNecessary(bufferIndex);
        return wasRecreated;
    }

    private boolean recreateBuffersIfNecessary(int buffersCount) {
        if (structureModified) {
            cleanupBuffer();
            initBuffers(buffersCount);
            structureModified = false;
            return true;
        }
        return false;
    }

    private void initBuffers(int buffersCount) {
        if (size > 0) {
            initBuffersInternal(buffersCount);
            contentModified = new ArrayList<>(buffersCount);
            for (int i = 0; i < buffersCount; i++) {
                contentModified.add(true);
            }
        }
        structureModified = false;
    }

    protected abstract void initBuffersInternal(int buffersCount);

    private void updateBufferIfNecessary(int bufferIndex) {
        if ((size > 0) && contentModified.get(bufferIndex)) {
            ByteBuffer byteBuffer = prepareUpdatingBuffer(bufferIndex);
            int index = 0;
            for (UniformValue<?> value : fields.values()) {
                value.write(byteBuffer, index);
                index += value.getAlignedSize();
            }
            finishUpdatingBuffer(bufferIndex);
            contentModified.set(bufferIndex, false);
        }
    }

    protected abstract ByteBuffer prepareUpdatingBuffer(int bufferIndex);

    protected abstract void finishUpdatingBuffer(int bufferIndex);

    @Override
    protected int getSize(UniformValue<?> uniformValue) {
        return uniformValue.getAlignedSize();
    }

    @Override
    public void cleanup() {
        cleanupBuffer();
        structureModified = true;
        super.cleanup();
    }

    protected abstract void cleanupBuffer();

    @Override
    public abstract BufferData clone(CloneContext context);
}