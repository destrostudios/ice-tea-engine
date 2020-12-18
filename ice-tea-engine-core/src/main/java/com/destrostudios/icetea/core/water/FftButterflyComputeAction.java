package com.destrostudios.icetea.core.water;

import com.destrostudios.icetea.core.*;
import com.destrostudios.icetea.core.materials.descriptors.ComputeImageDescriptor;
import com.destrostudios.icetea.core.materials.descriptors.ComputeImageDescriptorLayout;

import java.util.function.Function;

public class FftButterflyComputeAction extends ComputeAction {

    public FftButterflyComputeAction(Texture twiddleFactorsTexture, Texture pingPongTexture1, Texture pingPongTexture2) {
        this.twiddleFactorsTexture = twiddleFactorsTexture;
        this.pingPongTexture1 = pingPongTexture1;
        this.pingPongTexture2 = pingPongTexture2;
    }
    private Texture twiddleFactorsTexture;
    private Texture pingPongTexture1;
    private Texture pingPongTexture2;

    @Override
    protected void fillMaterialDescriptorSet(Function<Integer, MaterialDescriptorLayout> getDescriptorLayout) {
        materialDescriptorSet.addDescriptor(new ComputeImageDescriptor("twiddlesIndices", (ComputeImageDescriptorLayout) getDescriptorLayout.apply(0), twiddleFactorsTexture, "rgba32f", false));
        materialDescriptorSet.addDescriptor(new ComputeImageDescriptor("pingpong0", (ComputeImageDescriptorLayout) getDescriptorLayout.apply(1), pingPongTexture1, "rgba32f", false));
        materialDescriptorSet.addDescriptor(new ComputeImageDescriptor("pingpong1", (ComputeImageDescriptorLayout) getDescriptorLayout.apply(2), pingPongTexture2, "rgba32f", false));
    }
}
