package com.destrostudios.icetea.samples.water;

import com.destrostudios.icetea.core.compute.ComputeAction;
import com.destrostudios.icetea.core.resource.ResourceDescriptor;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class FftButterflyComputeAction extends ComputeAction {

    private ResourceDescriptor<?> twiddleFactorsTextureDescriptor;
    private ResourceDescriptor<?> pingPongTexture1Descriptor;
    private ResourceDescriptor<?> pingPongTexture2Descriptor;

    @Override
    protected void fillResourceDescriptorSet() {
        resourceDescriptorSet.setDescriptor("twiddlesIndices", twiddleFactorsTextureDescriptor);
        resourceDescriptorSet.setDescriptor("pingpong0", pingPongTexture1Descriptor);
        resourceDescriptorSet.setDescriptor("pingpong1", pingPongTexture2Descriptor);
    }
}
