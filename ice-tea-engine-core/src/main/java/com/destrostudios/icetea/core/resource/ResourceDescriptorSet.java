package com.destrostudios.icetea.core.resource;

import org.lwjgl.system.MemoryStack;

import java.nio.LongBuffer;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ResourceDescriptorSet {

    public ResourceDescriptorSet() {
        descriptors.put(ResourceReusability.HIGH, new LinkedHashMap<>());
        descriptors.put(ResourceReusability.MEDIUM, new LinkedHashMap<>());
        descriptors.put(ResourceReusability.LOW, new LinkedHashMap<>());
    }
    private LinkedHashMap<ResourceReusability, HashMap<String, ResourceDescriptor<?>>> descriptors = new LinkedHashMap<>();
    private boolean sortedDescriptorsOutdated;
    private List<Map.Entry<String, ResourceDescriptor<?>>> sortedDescriptorsEntries;
    private List<? extends ResourceDescriptor<?>> sortedDescriptors;
    private boolean shaderDeclarationOutdated;
    private String shaderDeclaration;

    public void setDescriptor(String name, ResourceDescriptor<?> descriptor) {
        setDescriptor(name, descriptor, ResourceReusability.MEDIUM);
    }

    public void setDescriptor(String name, ResourceDescriptor<?> descriptor, ResourceReusability volatility) {
        ResourceDescriptor<?> previousDescriptor = this.descriptors.get(volatility).put(name, descriptor);
        if (descriptor != previousDescriptor) {
            sortedDescriptorsOutdated = true;
            shaderDeclarationOutdated = true;
        }
    }

    public LongBuffer getDescriptorSetLayouts(MemoryStack stack) {
        LongBuffer descriptorSetLayouts = stack.mallocLong(size());
        int i = 0;
        for (ResourceDescriptor<?> descriptor : getSortedDescriptors()) {
            long descriptorSetLayout = descriptor.getDescriptorSetLayout();
            descriptorSetLayouts.put(i, descriptorSetLayout);
            i++;
        }
        return descriptorSetLayouts;
    }

    public LongBuffer getDescriptorSets(int descriptorStartIndex, int imageIndex, MemoryStack stack) {
        List<? extends ResourceDescriptor<?>> remainingDescriptors = getSortedDescriptors().subList(descriptorStartIndex, size());
        LongBuffer descriptorSets = stack.mallocLong(remainingDescriptors.size());
        int i = 0;
        for (ResourceDescriptor<?> descriptor : remainingDescriptors) {
            long descriptorSet = descriptor.getDescriptorSets()[imageIndex];
            descriptorSets.put(i, descriptorSet);
            i++;
        }
        return descriptorSets;
    }

    public long getDescriptorSet(int descriptorIndex, int imageIndex) {
        ResourceDescriptor<?> descriptor = getSortedDescriptors().get(descriptorIndex);
        return descriptor.getDescriptorSets()[imageIndex];
    }

    public String getShaderDeclaration() {
        if (shaderDeclarationOutdated) {
            shaderDeclaration = "";
            int setIndex = 0;
            for (Map.Entry<String, ResourceDescriptor<?>> entry : getSortedDescriptorsEntries()) {
                shaderDeclaration += entry.getValue().getShaderDeclaration(setIndex, entry.getKey()) + "\n\n";
                setIndex++;
            }
            shaderDeclarationOutdated = false;
        }
        return shaderDeclaration;
    }

    public int size() {
        return getSortedDescriptorsEntries().size();
    }

    private List<? extends ResourceDescriptor<?>> getSortedDescriptors() {
        updateSortedDescriptorsIfOutdated();
        return sortedDescriptors;
    }

    private List<Map.Entry<String, ResourceDescriptor<?>>> getSortedDescriptorsEntries() {
        updateSortedDescriptorsIfOutdated();
        return sortedDescriptorsEntries;
    }

    private void updateSortedDescriptorsIfOutdated() {
        if (sortedDescriptorsOutdated) {
            sortedDescriptorsEntries = descriptors.values().stream().flatMap(descriptors -> descriptors.entrySet().stream()).toList();
            sortedDescriptors = sortedDescriptorsEntries.stream().map(Map.Entry::getValue).toList();
            sortedDescriptorsOutdated = false;
        }
    }
}
