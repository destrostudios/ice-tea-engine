package com.destrostudios.icetea.core.materials.descriptors;

import com.destrostudios.icetea.core.MaterialDescriptor;
import com.destrostudios.icetea.core.UniformData;
import com.destrostudios.icetea.core.UniformValue;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VkDescriptorBufferInfo;
import org.lwjgl.vulkan.VkWriteDescriptorSet;

import java.util.List;
import java.util.Map;

import static org.lwjgl.vulkan.VK10.VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER;

public abstract class UniformDescriptor<LayoutType extends UniformDescriptorLayout> extends MaterialDescriptor<LayoutType> {

    public UniformDescriptor(String name, LayoutType layout, UniformData uniformData) {
        super(name, layout);
        this.uniformData = uniformData;
    }
    private UniformData uniformData;

    @Override
    public void initReferenceDescriptorWrite(VkWriteDescriptorSet descriptorWrite, MemoryStack stack) {
        descriptorWrite.descriptorType(VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER);
        VkDescriptorBufferInfo.Buffer descriptorBufferInfo = VkDescriptorBufferInfo.callocStack(1, stack);
        descriptorBufferInfo.offset(0);
        descriptorBufferInfo.range(uniformData.getSize());
        descriptorWrite.pBufferInfo(descriptorBufferInfo);
    }

    @Override
    public void updateReferenceDescriptorWrite(VkWriteDescriptorSet descriptorSet, int currentImage) {
        VkDescriptorBufferInfo.Buffer descriptorBufferInfo = descriptorSet.pBufferInfo();
        descriptorBufferInfo.buffer(uniformData.getUniformBuffers().get(currentImage));
    }

    @Override
    protected String getShaderDeclarationType() {
        String type = name.toUpperCase() + "_TYPE {\n";
        for (Map.Entry<String, UniformValue<?>> field : uniformData.getFields().entrySet()) {
            type += "    " + field.getValue().getShaderDefinitionType() + " " + field.getKey() + ";\n";
        }
        type += "}";
        return type;
    }

    @Override
    public List<String> getShaderDefines() {
        List<String> defines = super.getShaderDefines();
        for (String fieldName : uniformData.getFields().keySet()) {
            defines.add(name.toUpperCase() + "_" + fieldName.toUpperCase());
        }
        return defines;
    }
}