package com.destrostudios.icetea.core.resource;

import com.destrostudios.icetea.core.clone.CloneContext;
import com.destrostudios.icetea.core.clone.ContextCloneable;
import com.destrostudios.icetea.core.object.NativeObject;
import lombok.Getter;
import lombok.Setter;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.*;

import java.nio.LongBuffer;
import java.util.LinkedList;
import java.util.List;

import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.vulkan.VK10.*;

public abstract class ResourceDescriptor<R extends Resource> extends NativeObject implements ContextCloneable {

    public ResourceDescriptor() { }

    public ResourceDescriptor(ResourceDescriptor<R> resourceDescriptor, CloneContext context) {
        // Set parent-child relationships afterwards to avoid circular cloning
    }
    @Setter
    protected R resource;
    @Getter
    private Long descriptorSetLayout;
    private Long descriptorPool;
    @Getter
    private long[] descriptorSets;

    @Override
    protected void initNative() {
        super.initNative();
        // FIXME: Let the Resource or the ResourceDescriptorSet specify the amount of descriptorSets - Compute jobs only needs 1
        descriptorSets = new long[application.getSwapChain().getImages().size()];
        initDescriptorSetLayout();
        initDescriptorPool();
        initDescriptorSets();
    }

    private void initDescriptorSetLayout() {
        try (MemoryStack stack = stackPush()) {
            VkDescriptorSetLayoutCreateInfo layoutCreateInfo = VkDescriptorSetLayoutCreateInfo.callocStack(stack);
            layoutCreateInfo.sType(VK_STRUCTURE_TYPE_DESCRIPTOR_SET_LAYOUT_CREATE_INFO);

            VkDescriptorSetLayoutBinding.Buffer layoutBinding = VkDescriptorSetLayoutBinding.callocStack(1, stack);
            layoutBinding.binding(0);
            layoutBinding.descriptorCount(1);
            layoutBinding.descriptorType(getDescriptorType());
            initDescriptorSetLayoutBinding(layoutBinding);
            layoutCreateInfo.pBindings(layoutBinding);

            LongBuffer pDescriptorSetLayout = stack.mallocLong(1);
            int result = vkCreateDescriptorSetLayout(application.getLogicalDevice(), layoutCreateInfo, null, pDescriptorSetLayout);
            if (result != VK_SUCCESS) {
                throw new RuntimeException("Failed to create descriptor set layout (" + result + ")");
            }
            descriptorSetLayout = pDescriptorSetLayout.get(0);
        }
    }

    protected void initDescriptorSetLayoutBinding(VkDescriptorSetLayoutBinding.Buffer layoutBinding) {

    }

    private void initDescriptorPool() {
        try (MemoryStack stack = stackPush()) {
            VkDescriptorPoolCreateInfo poolCreateInfo = VkDescriptorPoolCreateInfo.callocStack(stack);
            poolCreateInfo.sType(VK_STRUCTURE_TYPE_DESCRIPTOR_POOL_CREATE_INFO);
            poolCreateInfo.maxSets(descriptorSets.length);
            poolCreateInfo.flags(VK_DESCRIPTOR_POOL_CREATE_FREE_DESCRIPTOR_SET_BIT);

            VkDescriptorPoolSize.Buffer poolSize = VkDescriptorPoolSize.callocStack(1, stack);
            poolSize.descriptorCount(descriptorSets.length);
            poolSize.type(getDescriptorType());
            poolCreateInfo.pPoolSizes(poolSize);

            LongBuffer pDescriptorPool = stack.mallocLong(1);
            int result = vkCreateDescriptorPool(application.getLogicalDevice(), poolCreateInfo, null, pDescriptorPool);
            if (result != VK_SUCCESS) {
                throw new RuntimeException("Failed to create descriptor pool (result = " + result + ")");
            }
            descriptorPool = pDescriptorPool.get(0);
        }
    }

    private void initDescriptorSets() {
        try (MemoryStack stack = stackPush()) {
            VkDescriptorSetAllocateInfo allocateInfo = VkDescriptorSetAllocateInfo.callocStack(stack);
            allocateInfo.sType(VK_STRUCTURE_TYPE_DESCRIPTOR_SET_ALLOCATE_INFO);
            allocateInfo.descriptorPool(descriptorPool);

            LongBuffer layouts = stack.mallocLong(descriptorSets.length);
            for (int i = 0; i < layouts.capacity(); i++) {
                layouts.put(i, descriptorSetLayout);
            }
            allocateInfo.pSetLayouts(layouts);

            LongBuffer pDescriptorSets = stack.mallocLong(descriptorSets.length);
            int result = vkAllocateDescriptorSets(application.getLogicalDevice(), allocateInfo, pDescriptorSets);
            if (result != VK_SUCCESS) {
                throw new RuntimeException("Failed to allocate descriptor sets (result = " + result + ")");
            }
            for (int i = 0; i < pDescriptorSets.capacity(); i++) {
                descriptorSets[i] = pDescriptorSets.get(i);
            }
        }
    }

    @Override
    public void updateNative() {
        super.updateNative();
        if (resource.isOutdated()) {
            updateDescriptorSets();
        }
    }

    protected void updateDescriptorSets() {
        try (MemoryStack stack = stackPush()) {
            VkWriteDescriptorSet.Buffer descriptorWrites = VkWriteDescriptorSet.callocStack(descriptorSets.length, stack);
            for (int i = 0; i < descriptorWrites.capacity(); i++) {
                VkWriteDescriptorSet descriptorWrite = descriptorWrites.get(i);
                descriptorWrite.sType(VK_STRUCTURE_TYPE_WRITE_DESCRIPTOR_SET);
                descriptorWrite.dstSet(descriptorSets[i]);
                descriptorWrite.dstBinding(0);
                descriptorWrite.descriptorType(getDescriptorType());
                descriptorWrite.descriptorCount(1);
                descriptorWrite.dstArrayElement(0);
                initWriteDescriptorSet(descriptorWrite, stack);
            }
            vkUpdateDescriptorSets(application.getLogicalDevice(), descriptorWrites, null);
        }
    }

    protected void initWriteDescriptorSet(VkWriteDescriptorSet descriptorWrite, MemoryStack stack) {

    }

    protected abstract int getDescriptorType();

    public String getShaderDeclaration(int setIndex, String name) {
        String text = "";
        List<String> defines = getShaderDeclaration_Defines(name);
        for (String define : defines) {
            text += "#define " + define + " 1\n";
        }
        text += "layout(set = " + setIndex + ", binding = 0";
        String layoutAddition = getShaderDeclaration_LayoutAddition();
        if (layoutAddition != null) {
            text += ", " + layoutAddition;
        }
        text += ") ";
        for (String keyword : getShaderDeclaration_Keywords()) {
            text += keyword + " ";
        }
        text += getShaderDeclaration_Type(name) + " " + name + ";";
        return text;
    }

    protected List<String> getShaderDeclaration_Defines(String name) {
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

    protected abstract String getShaderDeclaration_Type(String name);

    @Override
    protected void cleanupNativeInternal() {
        // TODO: Check if we need to do descriptorSets cleanup here
        descriptorSets = null;

        vkDestroyDescriptorPool(application.getLogicalDevice(), descriptorPool, null);
        descriptorPool = null;

        vkDestroyDescriptorSetLayout(application.getLogicalDevice(), descriptorSetLayout, null);
        descriptorSetLayout = null;

        super.cleanupNativeInternal();
    }

    @Override
    public abstract ResourceDescriptor<R> clone(CloneContext context);
}
