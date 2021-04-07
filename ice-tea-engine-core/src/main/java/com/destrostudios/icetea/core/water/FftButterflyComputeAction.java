package com.destrostudios.icetea.core.water;

import com.destrostudios.icetea.core.compute.ComputeAction;
import com.destrostudios.icetea.core.material.descriptor.ComputeImageDescriptor;
import com.destrostudios.icetea.core.texture.Texture;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class FftButterflyComputeAction extends ComputeAction {

    private Texture twiddleFactorsTexture;
    private Texture pingPongTexture1;
    private Texture pingPongTexture2;

    @Override
    protected void fillMaterialDescriptorSet() {
        materialDescriptorSet.addDescriptor(new ComputeImageDescriptor("twiddlesIndices", twiddleFactorsTexture, "rgba32f", false));
        materialDescriptorSet.addDescriptor(new ComputeImageDescriptor("pingpong0", pingPongTexture1, "rgba32f", false));
        materialDescriptorSet.addDescriptor(new ComputeImageDescriptor("pingpong1", pingPongTexture2, "rgba32f", false));
    }
}
