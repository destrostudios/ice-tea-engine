package com.destrostudios.icetea.core.data;

import com.destrostudios.icetea.core.Application;
import com.destrostudios.icetea.core.clone.CloneContext;
import com.destrostudios.icetea.core.data.values.UniformValue;
import lombok.Setter;
import org.lwjgl.system.MemoryStack;

import java.nio.ByteBuffer;
import java.util.ArrayList;

import static org.lwjgl.system.MemoryStack.stackPush;

public abstract class BufferData extends FieldsData {

    public BufferData() { }

    public BufferData(BufferData bufferData, CloneContext context) {
        super(bufferData, context);
    }
    @Setter
    protected Application application;

    @Override
    protected int getSize(UniformValue<?> uniformValue) {
        return uniformValue.getAlignedSize();
    }

    public boolean recreateBuffersIfNecessary(int buffersCount) {
        if (structureModified) {
            cleanupBuffer();
            initBuffers(buffersCount);
            structureModified = false;
            return true;
        }
        return false;
    }

    public void initBuffers(int buffersCount) {
        if (size > 0) {
            try (MemoryStack stack = stackPush()) {
                initBuffersInternal(buffersCount, stack);
                contentModified = new ArrayList<>(buffersCount);
                for (int i = 0; i < buffersCount; i++) {
                    contentModified.add(true);
                }
            }
        }
        structureModified = false;
    }

    protected abstract void initBuffersInternal(int buffersCount, MemoryStack stack);

    public void updateBufferIfNecessary(int bufferIndex) {
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

    public abstract void cleanupBuffer();

    @Override
    public abstract BufferData clone(CloneContext context);
}