package com.destrostudios.icetea.core.water;

import com.destrostudios.icetea.core.compute.ComputeAction;
import com.destrostudios.icetea.core.material.descriptor.MaterialDescriptorLayout;
import com.destrostudios.icetea.core.Texture;
import com.destrostudios.icetea.core.material.descriptor.ComputeImageDescriptor;
import com.destrostudios.icetea.core.material.descriptor.ComputeImageDescriptorLayout;

import java.util.function.Function;

public class FftInversionComputeAction extends ComputeAction {

    public FftInversionComputeAction(Texture displacementTexture, Texture coefficientsTexture, Texture pingPongTexture) {
        this.displacementTexture = displacementTexture;
        this.coefficientsTexture = coefficientsTexture;
        this.pingPongTexture = pingPongTexture;
    }
    private Texture displacementTexture;
    private Texture coefficientsTexture;
    private Texture pingPongTexture;

    @Override
    protected void fillMaterialDescriptorSet(Function<Integer, MaterialDescriptorLayout> getDescriptorLayout) {
        materialDescriptorSet.addDescriptor(new ComputeImageDescriptor("displacement", (ComputeImageDescriptorLayout) getDescriptorLayout.apply(0), displacementTexture, "rgba32f", false));
        materialDescriptorSet.addDescriptor(new ComputeImageDescriptor("pingpong0", (ComputeImageDescriptorLayout) getDescriptorLayout.apply(1), coefficientsTexture, "rgba32f", false));
        materialDescriptorSet.addDescriptor(new ComputeImageDescriptor("pingpong1", (ComputeImageDescriptorLayout) getDescriptorLayout.apply(2), pingPongTexture, "rgba32f", false));
    }
}
