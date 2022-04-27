package com.destrostudios.icetea.samples.water;

import com.destrostudios.icetea.core.compute.ComputeAction;
import com.destrostudios.icetea.core.resource.ResourceDescriptor;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class NormalMapComputeAction extends ComputeAction {

    private ResourceDescriptor<?> normalMapTextureDescriptor;
    private ResourceDescriptor<?> dyTextureDescriptor;

    @Override
    protected void fillResourceDescriptorSet() {
        resourceDescriptorSet.setDescriptor("normalMap", normalMapTextureDescriptor);
        resourceDescriptorSet.setDescriptor("heightMap", dyTextureDescriptor);
    }
}
