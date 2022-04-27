package com.destrostudios.icetea.samples.water;

import com.destrostudios.icetea.core.compute.ComputeAction;
import com.destrostudios.icetea.core.resource.ResourceDescriptor;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class FftInversionComputeAction extends ComputeAction {

    private ResourceDescriptor<?> displacementTextureDescriptor;
    private ResourceDescriptor<?> coefficientsTextureDescriptor;
    private ResourceDescriptor<?> pingPongTextureDescriptor;

    @Override
    protected void fillResourceDescriptorSet() {
        resourceDescriptorSet.setDescriptor("displacement", displacementTextureDescriptor);
        resourceDescriptorSet.setDescriptor("pingpong0", coefficientsTextureDescriptor);
        resourceDescriptorSet.setDescriptor("pingpong1", pingPongTextureDescriptor);
    }
}
