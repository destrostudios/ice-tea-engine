package com.destrostudios.icetea.samples.water;

import com.destrostudios.icetea.core.compute.ComputeAction;
import com.destrostudios.icetea.core.resource.ResourceDescriptor;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class HktComputeAction extends ComputeAction {

    private ResourceDescriptor<?> dxCoefficientsTextureDescriptorWrite;
    private ResourceDescriptor<?> dyCoefficientsTextureDescriptorWrite;
    private ResourceDescriptor<?> dzCoefficientsTextureDescriptorWrite;
    private ResourceDescriptor<?> h0kTextureDescriptorRead;
    private ResourceDescriptor<?> h0minuskTextureDescriptorRead;
    private ResourceDescriptor<?> uniformDescriptor;

    @Override
    protected void fillResourceDescriptorSet() {
        resourceDescriptorSet.setDescriptor("tilde_hkt_dx", dxCoefficientsTextureDescriptorWrite);
        resourceDescriptorSet.setDescriptor("tilde_hkt_dy", dyCoefficientsTextureDescriptorWrite);
        resourceDescriptorSet.setDescriptor("tilde_hkt_dz", dzCoefficientsTextureDescriptorWrite);
        resourceDescriptorSet.setDescriptor("tilde_h0k", h0kTextureDescriptorRead);
        resourceDescriptorSet.setDescriptor("tilde_h0minusk", h0minuskTextureDescriptorRead);
        resourceDescriptorSet.setDescriptor("constants", uniformDescriptor);
    }
}
