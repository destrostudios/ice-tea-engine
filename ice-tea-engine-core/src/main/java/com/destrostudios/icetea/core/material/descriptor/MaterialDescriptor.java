package com.destrostudios.icetea.core.material.descriptor;

import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VkDescriptorPoolSize;
import org.lwjgl.vulkan.VkWriteDescriptorSet;

import java.util.LinkedList;
import java.util.List;

public abstract class MaterialDescriptor<LayoutType extends MaterialDescriptorLayout> {

    public MaterialDescriptor(String name, LayoutType layout) {
        this.name = name;
        this.layout = layout;
    }
    protected String name;
    private LayoutType layout;

    public void initPoolSize(VkDescriptorPoolSize descriptorPoolSize) {
        descriptorPoolSize.type(layout.getDescriptorType());
    }

    public void initReferenceDescriptorWrite(VkWriteDescriptorSet descriptorWrite, MemoryStack stack) {
        descriptorWrite.descriptorType(layout.getDescriptorType());
    }

    public void updateReferenceDescriptorWrite(VkWriteDescriptorSet descriptorSet, int currentImage) {

    }

    public String getShaderDeclaration(int bindingIndex) {
        String text = "";
        List<String> defines = getShaderDeclaration_Defines();
        for (String define : defines) {
            text += "#define " + define + " 1\n";
        }
        text += "layout(binding = " + bindingIndex;
        String layoutAddition = getShaderDeclaration_LayoutAddition();
        if (layoutAddition != null) {
            text += ", " + layoutAddition;
        }
        text += ") ";
        for (String keyword : getShaderDeclaration_Keywords()) {
            text += keyword + " ";
        }
        text += getShaderDeclaration_Type() + " " + name + ";";
        return text;
    }

    protected List<String> getShaderDeclaration_Defines() {
        LinkedList<String> defines = new LinkedList<>();
        defines.add(name.toUpperCase());
        return defines;
    }

    protected String getShaderDeclaration_LayoutAddition() {
        return null;
    }

    protected List<String> getShaderDeclaration_Keywords() {
        return new LinkedList<>();
    }

    protected abstract String getShaderDeclaration_Type();
}