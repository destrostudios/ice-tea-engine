package com.destrostudios.icetea.core.resource;

import lombok.Getter;
import org.lwjgl.system.MemoryStack;

import java.nio.LongBuffer;
import java.util.HashMap;
import java.util.Map;

public class ResourceDescriptorSet {

    private HashMap<String, ResourceDescriptor<?>> descriptors = new HashMap<>();
    @Getter
    private boolean changed;

    public void setDescriptor(String name, ResourceDescriptor<?> descriptor) {
        ResourceDescriptor<?> previousDescriptor = descriptors.put(name, descriptor);
        if (descriptor != previousDescriptor) {
            changed = true;
        }
    }

    public void onChangeApplied() {
        changed = false;
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
        String text = "";
        int setIndex = 0;
        for (Map.Entry<String, ResourceDescriptor<?>> entry : descriptors.entrySet()) {
            text += entry.getValue().getShaderDeclaration(setIndex, entry.getKey()) + "\n\n";
            setIndex++;
        }
        return text;
    }
}
