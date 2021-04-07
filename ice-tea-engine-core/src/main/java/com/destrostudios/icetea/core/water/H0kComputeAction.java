package com.destrostudios.icetea.core.water;

import com.destrostudios.icetea.core.compute.ComputeAction;
import com.destrostudios.icetea.core.texture.Texture;
import com.destrostudios.icetea.core.data.UniformData;
import com.destrostudios.icetea.core.material.descriptor.ComputeImageDescriptor;
import com.destrostudios.icetea.core.material.descriptor.UniformDescriptor;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class H0kComputeAction extends ComputeAction {

    private Texture h0kTexture;
    private Texture h0minuskTexture;
    private Texture noiseTexture1;
    private Texture noiseTexture2;
    private Texture noiseTexture3;
    private Texture noiseTexture4;
    private UniformData uniformData;

    @Override
    protected void fillMaterialDescriptorSet() {
        materialDescriptorSet.addDescriptor(new ComputeImageDescriptor("tilde_h0k", h0kTexture, "rgba32f", true));
        materialDescriptorSet.addDescriptor(new ComputeImageDescriptor("tilde_h0minusk", h0minuskTexture, "rgba32f", true));
        materialDescriptorSet.addDescriptor(new ComputeImageDescriptor("noise_r0", noiseTexture1, "rgba8", false));
        materialDescriptorSet.addDescriptor(new ComputeImageDescriptor("noise_i0", noiseTexture2, "rgba8", false));
        materialDescriptorSet.addDescriptor(new ComputeImageDescriptor("noise_r1", noiseTexture3, "rgba8", false));
        materialDescriptorSet.addDescriptor(new ComputeImageDescriptor("noise_i1", noiseTexture4, "rgba8", false));
        materialDescriptorSet.addDescriptor(new UniformDescriptor("constants", uniformData));
    }
}
