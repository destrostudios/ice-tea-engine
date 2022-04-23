package com.destrostudios.icetea.core.material.descriptor;

import com.destrostudios.icetea.core.data.UniformData;
import com.destrostudios.icetea.core.data.values.UniformValue;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VkDescriptorBufferInfo;
import org.lwjgl.vulkan.VkWriteDescriptorSet;

import java.util.List;
import java.util.Map;

import static org.lwjgl.vulkan.VK10.VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER;

public class UniformDescriptor extends MaterialDescriptor {

    public UniformDescriptor(String name, UniformData uniformData) {
        super(name);
        this.uniformData = uniformData;
    }
    private UniformData uniformData;

    @Override
    public void initReferenceDescriptorWrite(VkWriteDescriptorSet descriptorWrite, MaterialDescriptorLayout layout, MemoryStack stack) {
        descriptorWrite.descriptorType(VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER);
        VkDescriptorBufferInfo.Buffer descriptorBufferInfo = VkDescriptorBufferInfo.callocStack(1, stack);
        descriptorBufferInfo.offset(0);
        descriptorBufferInfo.range(uniformData.getSize());
        descriptorWrite.pBufferInfo(descriptorBufferInfo);
    }

    @Override
    public void updateReferenceDescriptorWrite(VkWriteDescriptorSet descriptorSet, int currentImage) {
        VkDescriptorBufferInfo.Buffer descriptorBufferInfo = descriptorSet.pBufferInfo();
        descriptorBufferInfo.buffer(uniformData.getBuffer());
    }

    @Override
    protected String getShaderDeclaration_Type() {
        String type = "uniform " + name.toUpperCase() + "_TYPE {\n";
        for (Map.Entry<String, UniformValue<?>> field : uniformData.getFields().entrySet()) {
            type += "    " + field.getValue().getShaderDefinitionType() + " " + field.getKey() + ";\n";
        }
        type += "}";
        return type;
    }

    @Override
    protected List<String> getShaderDeclaration_Defines() {
        List<String> defines = super.getShaderDeclaration_Defines();
        for (String fieldName : uniformData.getFields().keySet()) {
            defines.add(name.toUpperCase() + "_" + fieldName.toUpperCase());
        }
        return defines;
    }
}