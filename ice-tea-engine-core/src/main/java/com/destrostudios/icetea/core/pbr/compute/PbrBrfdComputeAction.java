package com.destrostudios.icetea.core.pbr.compute;

import com.destrostudios.icetea.core.compute.ComputeAction;
import com.destrostudios.icetea.core.resource.ResourceDescriptor;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class PbrBrfdComputeAction extends ComputeAction {

    private ResourceDescriptor<?> brdfLookupTextureDescriptor;
    private ResourceDescriptor<?> uniformDescriptor;

    @Override
    protected void fillResourceDescriptorSet() {
        resourceDescriptorSet.setDescriptor("brdfLookupTexture", brdfLookupTextureDescriptor);
        resourceDescriptorSet.setDescriptor("uniforms", uniformDescriptor);
    }
}
