package com.destrostudios.icetea.core.buffer;

import com.destrostudios.icetea.core.clone.CloneContext;
import com.destrostudios.icetea.core.data.FieldsData;
import com.destrostudios.icetea.core.data.FieldsDataListener;
import com.destrostudios.icetea.core.data.values.UniformValue;
import com.destrostudios.icetea.core.resource.Resource;
import lombok.Getter;

import java.nio.ByteBuffer;

public abstract class DataBuffer extends Resource implements FieldsDataListener {

    public DataBuffer() {
        data = new FieldsData(UniformValue::getAlignedSize);
        data.setListener(this);
    }

    public DataBuffer(DataBuffer dataBuffer, CloneContext context) {
        super(dataBuffer, context);
        data = dataBuffer.data.clone(context);
        data.setListener(this);
        structureModified = true;
        contentModified = true;
    }
    @Getter
    protected FieldsData data;
    private boolean structureModified;
    private boolean contentModified;

    @Override
    public void onFieldValueAdded(UniformValue<?> uniformValue) {
        structureModified = true;
    }

    @Override
    public void onFieldValueSet(UniformValue<?> uniformValue) {
        contentModified = true;
    }

    @Override
    public void onFieldValueRemoved(UniformValue<?> uniformValue) {
        structureModified = true;
        contentModified = true;
    }

    @Override
    protected void updateResource() {
        recreateBufferIfNecessary();
        updateBufferIfNecessary();
    }

    private void recreateBufferIfNecessary() {
        if (structureModified) {
            cleanupBuffer();
            if (data.getSize() > 0) {
                initBufferInternal();
                contentModified = true;
                setWasOutdated();
            }
            structureModified = false;
        }
    }

    protected abstract void initBufferInternal();

    private void updateBufferIfNecessary() {
        if ((data.getSize() > 0) && contentModified) {
            ByteBuffer byteBuffer = prepareUpdatingBuffer();
            int index = 0;
            for (UniformValue<?> value : data.getFields().values()) {
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
    protected void cleanupInternal() {
        cleanupBuffer();
        structureModified = true;
        contentModified = true;
        data.cleanup();
        super.cleanupInternal();
    }

    protected abstract void cleanupBuffer();

    public abstract DataBuffer clone(CloneContext context);
}