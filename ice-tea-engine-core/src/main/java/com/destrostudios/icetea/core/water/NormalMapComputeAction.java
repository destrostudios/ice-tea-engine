package com.destrostudios.icetea.core.water;

import com.destrostudios.icetea.core.compute.ComputeAction;
import com.destrostudios.icetea.core.material.descriptor.ComputeImageDescriptor;
import com.destrostudios.icetea.core.material.descriptor.NormalMapDescriptor;
import com.destrostudios.icetea.core.texture.Texture;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class NormalMapComputeAction extends ComputeAction {

    private Texture normalMapTexture;
    private Texture dyTexture;

    @Override
    protected void fillMaterialDescriptorSet() {
        materialDescriptorSet.addDescriptor(new ComputeImageDescriptor("normalMap", normalMapTexture, "rgba32f", true));
        materialDescriptorSet.addDescriptor(new NormalMapDescriptor("heightMap", dyTexture));
    }
}
