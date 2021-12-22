package com.destrostudios.icetea.samples.water;

import com.destrostudios.icetea.core.compute.ComputeAction;
import com.destrostudios.icetea.core.texture.Texture;
import com.destrostudios.icetea.core.material.descriptor.ComputeImageDescriptor;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class FftInversionComputeAction extends ComputeAction {

    private Texture displacementTexture;
    private Texture coefficientsTexture;
    private Texture pingPongTexture;

    @Override
    protected void fillMaterialDescriptorSet() {
        materialDescriptorSet.addDescriptor(new ComputeImageDescriptor("displacement", displacementTexture, "rgba32f", false));
        materialDescriptorSet.addDescriptor(new ComputeImageDescriptor("pingpong0", coefficientsTexture, "rgba32f", false));
        materialDescriptorSet.addDescriptor(new ComputeImageDescriptor("pingpong1", pingPongTexture, "rgba32f", false));
    }
}
