package com.destrostudios.icetea.samples.water;

import com.destrostudios.icetea.core.compute.ComputeAction;
import com.destrostudios.icetea.core.data.UniformData;
import com.destrostudios.icetea.core.material.descriptor.ComputeImageDescriptor;
import com.destrostudios.icetea.core.material.descriptor.UniformDescriptor;
import com.destrostudios.icetea.core.texture.Texture;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class HktComputeAction extends ComputeAction {

    private Texture dyCoefficientsTexture;
    private Texture dxCoefficientsTexture;
    private Texture dzCoefficientsTexture;
    private Texture h0kTexture;
    private Texture h0minuskTexture;
    private UniformData uniformData;

    @Override
    protected void fillMaterialDescriptorSet() {
        materialDescriptorSet.addDescriptor(new ComputeImageDescriptor("tilde_hkt_dy", dyCoefficientsTexture, "rgba32f", true));
        materialDescriptorSet.addDescriptor(new ComputeImageDescriptor("tilde_hkt_dx", dxCoefficientsTexture, "rgba32f", true));
        materialDescriptorSet.addDescriptor(new ComputeImageDescriptor("tilde_hkt_dz", dzCoefficientsTexture, "rgba32f", true));
        materialDescriptorSet.addDescriptor(new ComputeImageDescriptor("tilde_h0k", h0kTexture, "rgba32f", false));
        materialDescriptorSet.addDescriptor(new ComputeImageDescriptor("tilde_h0minusk", h0minuskTexture, "rgba32f", false));
        materialDescriptorSet.addDescriptor(new UniformDescriptor("constants", uniformData));
    }
}
