package com.destrostudios.icetea.core.material.descriptor;

import com.destrostudios.icetea.core.data.StorageBufferData;
import com.destrostudios.icetea.core.data.values.UniformValue;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VkDescriptorBufferInfo;
import org.lwjgl.vulkan.VkWriteDescriptorSet;

import java.util.List;
import java.util.Map;

import static org.lwjgl.vulkan.VK10.VK_DESCRIPTOR_TYPE_STORAGE_BUFFER;

public class StorageBufferDescriptor extends MaterialDescriptor<StorageBufferDescriptorLayout> {

    public StorageBufferDescriptor(String name, StorageBufferDescriptorLayout layout, StorageBufferData storageBufferData) {
        super(name, layout);
        this.storageBufferData = storageBufferData;
    }
    private StorageBufferData storageBufferData;

    @Override
    public void initReferenceDescriptorWrite(VkWriteDescriptorSet descriptorWrite, MemoryStack stack) {
        descriptorWrite.descriptorType(VK_DESCRIPTOR_TYPE_STORAGE_BUFFER);
        VkDescriptorBufferInfo.Buffer descriptorBufferInfo = VkDescriptorBufferInfo.callocStack(1, stack);
        descriptorBufferInfo.offset(0);
        descriptorBufferInfo.range(storageBufferData.getSize());
        descriptorWrite.pBufferInfo(descriptorBufferInfo);
    }

    @Override
    public void updateReferenceDescriptorWrite(VkWriteDescriptorSet descriptorSet, int currentImage) {
        VkDescriptorBufferInfo.Buffer descriptorBufferInfo = descriptorSet.pBufferInfo();
        descriptorBufferInfo.buffer(storageBufferData.getBuffers().get(0));
    }

    @Override
    protected String getShaderDeclaration_Type() {
        String type = "";
        type += "buffer " + name.toUpperCase() + "_TYPE {\n";
        for (Map.Entry<String, UniformValue<?>> field : storageBufferData.getFields().entrySet()) {
            type += "    " + field.getValue().getShaderDefinitionType() + " " + field.getKey() + ";\n";
        }
        type += "}";
        return type;
    }

    @Override
    protected List<String> getShaderDeclaration_Defines() {
        List<String> defines = super.getShaderDeclaration_Defines();
        return defines;
    }
}