package com.destrostudios.icetea.core.buffer;

import com.destrostudios.icetea.core.clone.CloneContext;
import com.destrostudios.icetea.core.data.FieldsData;
import com.destrostudios.icetea.core.data.FieldsDataListener;
import com.destrostudios.icetea.core.data.values.UniformValue;
import com.destrostudios.icetea.core.resource.Resource;
import lombok.Getter;

public abstract class FieldsDataBuffer<RB extends ResizableBuffer> extends Resource implements FieldsDataListener {

    public FieldsDataBuffer(RB buffer) {
        data = new FieldsData(UniformValue::getAlignedSize);
        data.setListener(this);
        this.buffer = buffer;
    }

    public FieldsDataBuffer(FieldsDataBuffer<RB> fieldsDataBuffer, CloneContext context) {
        super(fieldsDataBuffer, context);
        data = fieldsDataBuffer.data.clone(context);
        data.setListener(this);
        buffer = (RB) fieldsDataBuffer.buffer.clone(context);
        contentModified = true;
    }
    @Getter
    protected FieldsData data;
    @Getter
    protected RB buffer;
    private boolean contentModified;

    @Override
    public void onFieldsDataModified(UniformValue<?> uniformValue) {
        contentModified = true;
    }

    @Override
    protected void updateResource(float tpf) {
        buffer.update(application, tpf);
        if (contentModified) {
            boolean resized = buffer.write(data.getSize(), byteBuffer -> {
                int index = 0;
                for (UniformValue<?> value : data.getFields().values()) {
                    value.write(byteBuffer, index);
                    index += value.getAlignedSize();
                }
            });
            contentModified = false;
            if (resized) {
                setWasOutdated();
            }
        }
    }

    @Override
    protected void cleanupInternal() {
        contentModified = true;
        buffer.cleanup();
        data.cleanup();
        super.cleanupInternal();
    }

    public abstract FieldsDataBuffer<RB> clone(CloneContext context);
}