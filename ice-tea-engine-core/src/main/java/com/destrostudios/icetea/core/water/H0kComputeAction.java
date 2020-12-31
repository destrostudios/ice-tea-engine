package com.destrostudios.icetea.core.water;

import com.destrostudios.icetea.core.compute.ComputeAction;
import com.destrostudios.icetea.core.material.descriptor.MaterialDescriptorLayout;
import com.destrostudios.icetea.core.Texture;
import com.destrostudios.icetea.core.data.UniformData;
import com.destrostudios.icetea.core.material.descriptor.ComputeImageDescriptor;
import com.destrostudios.icetea.core.material.descriptor.ComputeImageDescriptorLayout;
import com.destrostudios.icetea.core.material.descriptor.UniformDescriptor;
import com.destrostudios.icetea.core.material.descriptor.UniformDescriptorLayout;

import java.util.function.Function;

public class H0kComputeAction extends ComputeAction {

    public H0kComputeAction(Texture h0kTexture, Texture h0minuskTexture, Texture noiseTexture1, Texture noiseTexture2, Texture noiseTexture3, Texture noiseTexture4, UniformData uniformData) {
        this.h0kTexture = h0kTexture;
        this.h0minuskTexture = h0minuskTexture;
        this.noiseTexture1 = noiseTexture1;
        this.noiseTexture2 = noiseTexture2;
        this.noiseTexture3 = noiseTexture3;
        this.noiseTexture4 = noiseTexture4;
        this.uniformData = uniformData;
    }
    private Texture h0kTexture;
    private Texture h0minuskTexture;
    private Texture noiseTexture1;
    private Texture noiseTexture2;
    private Texture noiseTexture3;
    private Texture noiseTexture4;
    private UniformData uniformData;

    @Override
    protected void fillMaterialDescriptorSet(Function<Integer, MaterialDescriptorLayout> getDescriptorLayout) {
        materialDescriptorSet.addDescriptor(new ComputeImageDescriptor("tilde_h0k", (ComputeImageDescriptorLayout) getDescriptorLayout.apply(0), h0kTexture, "rgba32f", true));
        materialDescriptorSet.addDescriptor(new ComputeImageDescriptor("tilde_h0minusk", (ComputeImageDescriptorLayout) getDescriptorLayout.apply(1), h0minuskTexture, "rgba32f", true));
        materialDescriptorSet.addDescriptor(new ComputeImageDescriptor("noise_r0", (ComputeImageDescriptorLayout) getDescriptorLayout.apply(2), noiseTexture1, "rgba8", false));
        materialDescriptorSet.addDescriptor(new ComputeImageDescriptor("noise_i0", (ComputeImageDescriptorLayout) getDescriptorLayout.apply(3), noiseTexture2, "rgba8", false));
        materialDescriptorSet.addDescriptor(new ComputeImageDescriptor("noise_r1", (ComputeImageDescriptorLayout) getDescriptorLayout.apply(4), noiseTexture3, "rgba8", false));
        materialDescriptorSet.addDescriptor(new ComputeImageDescriptor("noise_i1", (ComputeImageDescriptorLayout) getDescriptorLayout.apply(5), noiseTexture4, "rgba8", false));
        materialDescriptorSet.addDescriptor(new UniformDescriptor<>("constants", (UniformDescriptorLayout) getDescriptorLayout.apply(6), uniformData));
    }
}
