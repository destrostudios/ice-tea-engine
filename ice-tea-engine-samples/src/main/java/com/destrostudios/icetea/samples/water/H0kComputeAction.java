package com.destrostudios.icetea.samples.water;

import com.destrostudios.icetea.core.compute.ComputeAction;
import com.destrostudios.icetea.core.resource.ResourceDescriptor;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class H0kComputeAction extends ComputeAction {

    private ResourceDescriptor<?> h0kTextureDescriptorWrite;
    private ResourceDescriptor<?> h0minuskTextureDescriptorWrite;
    private ResourceDescriptor<?> noiseTexture1Descriptor;
    private ResourceDescriptor<?> noiseTexture2Descriptor;
    private ResourceDescriptor<?> noiseTexture3Descriptor;
    private ResourceDescriptor<?> noiseTexture4Descriptor;
    private ResourceDescriptor<?> uniformDescriptor;

    @Override
    protected void fillResourceDescriptorSet() {
        resourceDescriptorSet.setDescriptor("tilde_h0k", h0kTextureDescriptorWrite);
        resourceDescriptorSet.setDescriptor("tilde_h0minusk", h0minuskTextureDescriptorWrite);
        resourceDescriptorSet.setDescriptor("noise_r0", noiseTexture1Descriptor);
        resourceDescriptorSet.setDescriptor("noise_i0", noiseTexture2Descriptor);
        resourceDescriptorSet.setDescriptor("noise_r1", noiseTexture3Descriptor);
        resourceDescriptorSet.setDescriptor("noise_i1", noiseTexture4Descriptor);
        resourceDescriptorSet.setDescriptor("constants", uniformDescriptor);
    }
}
