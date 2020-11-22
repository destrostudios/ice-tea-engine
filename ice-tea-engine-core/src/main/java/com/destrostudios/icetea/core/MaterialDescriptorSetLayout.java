package com.destrostudios.icetea.core;

import lombok.Getter;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.*;

import java.nio.LongBuffer;
import java.util.LinkedList;
import java.util.List;

import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.vulkan.VK10.*;

public class MaterialDescriptorSetLayout {

    public MaterialDescriptorSetLayout(Application application) {
        this.application = application;
        descriptorLayouts = new LinkedList<>();
    }
    private Application application;
    @Getter
    protected Long descriptorSetLayout;
    private List<MaterialDescriptorLayout> descriptorLayouts;

    public void addDescriptorLayout(MaterialDescriptorLayout descriptorLayout) {
        descriptorLayouts.add(descriptorLayout);
    }

    public void initDescriptorSetLayout() {
        try (MemoryStack stack = stackPush()) {
            VkDescriptorSetLayoutBinding.Buffer bindings = VkDescriptorSetLayoutBinding.callocStack(getDescriptorsCount(), stack);

            int descriptorIndex = 0;
            for (MaterialDescriptorLayout descriptorLayout : descriptorLayouts) {
                VkDescriptorSetLayoutBinding descriptorLayoutBinding = bindings.get(descriptorIndex);
                descriptorLayoutBinding.descriptorType(descriptorLayout.getDescriptorType());
                descriptorLayoutBinding.binding(descriptorIndex);
                descriptorLayoutBinding.descriptorCount(1);
                descriptorLayout.initLayoutBinding(descriptorLayoutBinding);
                descriptorIndex++;
            }

            VkDescriptorSetLayoutCreateInfo layoutCreateInfo = VkDescriptorSetLayoutCreateInfo.callocStack(stack);
            layoutCreateInfo.sType(VK_STRUCTURE_TYPE_DESCRIPTOR_SET_LAYOUT_CREATE_INFO);
            layoutCreateInfo.pBindings(bindings);

            LongBuffer pDescriptorSetLayout = stack.mallocLong(1);
            if (vkCreateDescriptorSetLayout(application.getLogicalDevice(), layoutCreateInfo, null, pDescriptorSetLayout) != VK_SUCCESS) {
                throw new RuntimeException("Failed to create descriptor set layout");
            }
            descriptorSetLayout = pDescriptorSetLayout.get(0);
        }
    }

    public void cleanupDescriptorSetLayout() {
        if (descriptorSetLayout != null) {
            vkDestroyDescriptorSetLayout(application.getLogicalDevice(), descriptorSetLayout, null);
            descriptorSetLayout = null;
        }
    }

    public <T extends MaterialDescriptorLayout> T getDescriptorLayout(int index) {
        return (T) descriptorLayouts.get(index);
    }

    public int getDescriptorsCount() {
        return descriptorLayouts.size();
    }
}
