package com.destrostudios.icetea.core.resource;

import org.lwjgl.system.MemoryStack;

import java.nio.LongBuffer;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ResourceDescriptorSet {

    private LinkedHashMap<String, ResourceDescriptor<?>> descriptors = new LinkedHashMap<>();
    private boolean shaderDeclarationOutdated;
    private String shaderDeclaration;

    public void setDescriptor(String name, ResourceDescriptor<?> descriptor) {
        ResourceDescriptor<?> previousDescriptor = descriptors.put(name, descriptor);
        if (descriptor != previousDescriptor) {
            shaderDeclarationOutdated = true;
        }
    }

    public LongBuffer getDescriptorSetLayouts(MemoryStack stack) {
        LongBuffer descriptorSetLayouts = stack.mallocLong(descriptors.size());
        int i = 0;
        for (ResourceDescriptor<?> descriptor : descriptors.values()) {
            long descriptorSetLayout = descriptor.getDescriptorSetLayout();
            descriptorSetLayouts.put(i, descriptorSetLayout);
            i++;
        }
        return descriptorSetLayouts;
    }

    public LongBuffer getDescriptorSets(int descriptorStartIndex, int imageIndex, MemoryStack stack) {
        List<ResourceDescriptor<?>> subDescriptors = descriptors.values().stream().skip(descriptorStartIndex).toList();
        LongBuffer descriptorSets = stack.mallocLong(subDescriptors.size());
        int i = 0;
        for (ResourceDescriptor<?> descriptor : subDescriptors) {
            long descriptorSet = descriptor.getDescriptorSets()[imageIndex];
            descriptorSets.put(i, descriptorSet);
            i++;
        }
        return descriptorSets;
    }

    public long getDescriptorSet(int descriptorIndex, int imageIndex) {
        ResourceDescriptor<?> descriptor = descriptors.values().stream().skip(descriptorIndex).findFirst().get();
        return descriptor.getDescriptorSets()[imageIndex];
    }

    public int size() {
        return descriptors.size();
    }

    public String getShaderDeclaration() {
        if (shaderDeclarationOutdated) {
            shaderDeclaration = "";
            int setIndex = 0;
            for (Map.Entry<String, ResourceDescriptor<?>> entry : descriptors.entrySet()) {
                shaderDeclaration += entry.getValue().getShaderDeclaration(setIndex, entry.getKey()) + "\n\n";
                setIndex++;
            }
            shaderDeclarationOutdated = false;
        }
        return shaderDeclaration;
    }
}
