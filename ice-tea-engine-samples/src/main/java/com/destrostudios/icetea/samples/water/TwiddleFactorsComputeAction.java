package com.destrostudios.icetea.samples.water;

import com.destrostudios.icetea.core.compute.ComputeAction;
import com.destrostudios.icetea.core.resource.ResourceDescriptor;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class TwiddleFactorsComputeAction extends ComputeAction {

    private ResourceDescriptor<?> twiddleFactorsTextureDescriptor;
    private ResourceDescriptor<?> storageBufferDescriptor;
    private ResourceDescriptor<?> uniformDescriptor;

    @Override
    protected void fillResourceDescriptorSet() {
        resourceDescriptorSet.setDescriptor("twiddleIndices", twiddleFactorsTextureDescriptor);
        resourceDescriptorSet.setDescriptor("myBuffer", storageBufferDescriptor);
        resourceDescriptorSet.setDescriptor("uniforms", uniformDescriptor);
    }
}
