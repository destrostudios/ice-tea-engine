package com.destrostudios.icetea.core.resource.descriptor;

import com.destrostudios.icetea.core.buffer.MemoryDataBuffer;
import com.destrostudios.icetea.core.clone.CloneContext;
import com.destrostudios.icetea.core.resource.ResourceDescriptor;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VkDescriptorBufferInfo;
import org.lwjgl.vulkan.VkWriteDescriptorSet;

public abstract class MemoryBufferDataDescriptor<B extends MemoryDataBuffer> extends ResourceDescriptor<B> {

    public MemoryBufferDataDescriptor(int descriptorType, int stageFlags) {
        super(descriptorType, stageFlags);
    }

    public MemoryBufferDataDescriptor(MemoryBufferDataDescriptor<B> memoryBufferDataDescriptor, CloneContext context) {
        super(memoryBufferDataDescriptor, context);
    }

    @Override
    protected void initWriteDescriptorSet(VkWriteDescriptorSet descriptorWrite, MemoryStack stack) {
        super.initWriteDescriptorSet(descriptorWrite, stack);
        VkDescriptorBufferInfo.Buffer descriptorBufferInfo = VkDescriptorBufferInfo.callocStack(1, stack);
        descriptorBufferInfo.buffer(resource.getBuffer().getBuffer());
        descriptorBufferInfo.offset(0);
        descriptorBufferInfo.range(resource.getData().getSize());
        descriptorWrite.pBufferInfo(descriptorBufferInfo);
    }

    @Override
    public abstract MemoryBufferDataDescriptor<B> clone(CloneContext context);
}