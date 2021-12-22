package com.destrostudios.icetea.core.material.descriptor;

import com.destrostudios.icetea.core.Application;
import lombok.Getter;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.*;

import java.nio.LongBuffer;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.vulkan.VK10.*;

public class MaterialDescriptorSet {

    public MaterialDescriptorSet(Application application, MaterialDescriptorSetLayout setLayout, int descriptorSetsCount) {
        this.application = application;
        this.setLayout = setLayout;
        this.descriptorSetsCount = descriptorSetsCount;
        descriptors = new LinkedList<>();
    }
    private Application application;
    @Getter
    private MaterialDescriptorSetLayout setLayout;
    private int descriptorSetsCount;
    private List<MaterialDescriptor> descriptors;

    public void addDescriptor(MaterialDescriptor descriptor) {
        descriptors.add(descriptor);
    }

    public long createDescriptorPool() {
        try (MemoryStack stack = stackPush()) {
            VkDescriptorPoolSize.Buffer poolSizes = VkDescriptorPoolSize.callocStack(setLayout.getDescriptorsCount(), stack);

            int descriptorIndex = 0;
            for (MaterialDescriptor descriptor : descriptors) {
                VkDescriptorPoolSize descriptorsPoolSize = poolSizes.get(descriptorIndex);
                descriptorsPoolSize.descriptorCount(descriptorSetsCount);
                descriptor.initPoolSize(descriptorsPoolSize, setLayout.getDescriptorLayout(descriptorIndex));
                descriptorIndex++;
            }

            VkDescriptorPoolCreateInfo poolCreateInfo = VkDescriptorPoolCreateInfo.callocStack(stack);
            poolCreateInfo.sType(VK_STRUCTURE_TYPE_DESCRIPTOR_POOL_CREATE_INFO);
            poolCreateInfo.pPoolSizes(poolSizes);
            poolCreateInfo.maxSets(descriptorSetsCount);

            LongBuffer pDescriptorPool = stack.mallocLong(1);
            int result = vkCreateDescriptorPool(application.getLogicalDevice(), poolCreateInfo, null, pDescriptorPool);
            if (result != VK_SUCCESS) {
                throw new RuntimeException("Failed to create descriptor pool (result = " + result + ")");
            }
            return pDescriptorPool.get(0);
        }
    }

    public void cleanupDescriptorPool(long descriptorPool) {
        vkDestroyDescriptorPool(application.getLogicalDevice(), descriptorPool, null);
    }

    public List<Long> createDescriptorSets(long descriptorPool) {
        try (MemoryStack stack = stackPush()) {
            VkDescriptorSetAllocateInfo allocateInfo = VkDescriptorSetAllocateInfo.callocStack(stack);
            allocateInfo.sType(VK_STRUCTURE_TYPE_DESCRIPTOR_SET_ALLOCATE_INFO);
            allocateInfo.descriptorPool(descriptorPool);

            LongBuffer layouts = stack.mallocLong(descriptorSetsCount);
            for (int i = 0; i < layouts.capacity(); i++) {
                layouts.put(i, setLayout.getDescriptorSetLayout());
            }
            allocateInfo.pSetLayouts(layouts);

            LongBuffer pDescriptorSets = stack.mallocLong(descriptorSetsCount);
            int result = vkAllocateDescriptorSets(application.getLogicalDevice(), allocateInfo, pDescriptorSets);
            if (result != VK_SUCCESS) {
                throw new RuntimeException("Failed to allocate descriptor sets (result = " + result + ")");
            }

            VkWriteDescriptorSet.Buffer descriptorWrites = VkWriteDescriptorSet.callocStack(setLayout.getDescriptorsCount(), stack);

            int descriptorIndex = 0;
            for (MaterialDescriptor descriptor : descriptors) {
                VkWriteDescriptorSet descriptorWrite = descriptorWrites.get(descriptorIndex);
                descriptorWrite.sType(VK_STRUCTURE_TYPE_WRITE_DESCRIPTOR_SET);
                descriptorWrite.dstBinding(descriptorIndex);
                descriptorWrite.dstArrayElement(0);
                descriptorWrite.descriptorCount(1);
                descriptor.initReferenceDescriptorWrite(descriptorWrite, setLayout.getDescriptorLayout(descriptorIndex), stack);
                descriptorIndex++;
            }

            ArrayList<Long> descriptorSets = new ArrayList<>(pDescriptorSets.capacity());
            for (int i = 0; i < pDescriptorSets.capacity(); i++) {
                long descriptorSet = pDescriptorSets.get(i);

                descriptorIndex = 0;
                for (MaterialDescriptor descriptor : descriptors) {
                    VkWriteDescriptorSet descriptorWrite = descriptorWrites.get(descriptorIndex);
                    descriptorWrite.dstSet(descriptorSet);
                    descriptor.updateReferenceDescriptorWrite(descriptorWrite, i);
                    descriptorIndex++;
                }

                vkUpdateDescriptorSets(application.getLogicalDevice(), descriptorWrites, null);
                descriptorSets.add(descriptorSet);
            }
            return descriptorSets;
        }
    }

    public void cleanupDescriptorSets(long descriptorPool, List<Long> descriptorSets) {
        for (long descriptorSet : descriptorSets) {
            vkFreeDescriptorSets(application.getLogicalDevice(), descriptorPool, descriptorSet);
        }
    }

    public String getShaderDeclaration() {
        String text = "";
        int bindingIndex = 0;
        for (MaterialDescriptor descriptor : descriptors) {
            text += descriptor.getShaderDeclaration(bindingIndex) + "\n\n";
            bindingIndex++;
        }
        return text;
    }
}
