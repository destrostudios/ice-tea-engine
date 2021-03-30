package com.destrostudios.icetea.core.water;

import com.destrostudios.icetea.core.compute.ComputeAction;
import com.destrostudios.icetea.core.material.descriptor.MaterialDescriptorLayout;
import com.destrostudios.icetea.core.texture.Texture;
import com.destrostudios.icetea.core.data.UniformData;
import com.destrostudios.icetea.core.material.descriptor.ComputeImageDescriptor;
import com.destrostudios.icetea.core.material.descriptor.ComputeImageDescriptorLayout;
import com.destrostudios.icetea.core.material.descriptor.UniformDescriptor;
import com.destrostudios.icetea.core.material.descriptor.UniformDescriptorLayout;

import java.util.function.Function;

public class HktComputeAction extends ComputeAction {

    public HktComputeAction(Texture dyCoefficientsTexture, Texture dxCoefficientsTexture, Texture dzCoefficientsTexture, Texture h0kTexture, Texture h0minuskTexture, UniformData uniformData) {
        this.dyCoefficientsTexture = dyCoefficientsTexture;
        this.dxCoefficientsTexture = dxCoefficientsTexture;
        this.dzCoefficientsTexture = dzCoefficientsTexture;
        this.h0kTexture = h0kTexture;
        this.h0minuskTexture = h0minuskTexture;
        this.uniformData = uniformData;
    }
    private Texture dyCoefficientsTexture;
    private Texture dxCoefficientsTexture;
    private Texture dzCoefficientsTexture;
    private Texture h0kTexture;
    private Texture h0minuskTexture;
    private UniformData uniformData;

    @Override
    protected void fillMaterialDescriptorSet(Function<Integer, MaterialDescriptorLayout> getDescriptorLayout) {
        materialDescriptorSet.addDescriptor(new ComputeImageDescriptor("tilde_hkt_dy", (ComputeImageDescriptorLayout) getDescriptorLayout.apply(0), dyCoefficientsTexture, "rgba32f", true));
        materialDescriptorSet.addDescriptor(new ComputeImageDescriptor("tilde_hkt_dx", (ComputeImageDescriptorLayout) getDescriptorLayout.apply(1), dxCoefficientsTexture, "rgba32f", true));
        materialDescriptorSet.addDescriptor(new ComputeImageDescriptor("tilde_hkt_dz", (ComputeImageDescriptorLayout) getDescriptorLayout.apply(2), dzCoefficientsTexture, "rgba32f", true));
        materialDescriptorSet.addDescriptor(new ComputeImageDescriptor("tilde_h0k", (ComputeImageDescriptorLayout) getDescriptorLayout.apply(3), h0kTexture, "rgba32f", false));
        materialDescriptorSet.addDescriptor(new ComputeImageDescriptor("tilde_h0minusk", (ComputeImageDescriptorLayout) getDescriptorLayout.apply(4), h0minuskTexture, "rgba32f", false));
        materialDescriptorSet.addDescriptor(new UniformDescriptor<>("constants", (UniformDescriptorLayout) getDescriptorLayout.apply(5), uniformData));
    }
}
