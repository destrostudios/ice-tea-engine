package com.destrostudios.icetea.core.resource;

import org.lwjgl.system.MemoryStack;

import java.nio.LongBuffer;
import java.util.HashMap;
import java.util.Map;

public class ResourceDescriptorSet {

    private HashMap<String, ResourceDescriptor<?>> descriptors = new HashMap<>();
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

    public LongBuffer getDescriptorSets(int imageIndex, MemoryStack stack) {
        LongBuffer descriptorSets = stack.mallocLong(descriptors.size());
        int i = 0;
        for (ResourceDescriptor<?> descriptor : descriptors.values()) {
            long descriptorSet = descriptor.getDescriptorSets()[imageIndex];
            descriptorSets.put(i, descriptorSet);
            i++;
        }
        return descriptorSets;
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
