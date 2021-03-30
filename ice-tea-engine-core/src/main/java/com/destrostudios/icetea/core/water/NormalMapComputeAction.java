package com.destrostudios.icetea.core.water;

import com.destrostudios.icetea.core.compute.ComputeAction;
import com.destrostudios.icetea.core.material.descriptor.MaterialDescriptorLayout;
import com.destrostudios.icetea.core.texture.Texture;
import com.destrostudios.icetea.core.material.descriptor.ComputeImageDescriptor;
import com.destrostudios.icetea.core.material.descriptor.ComputeImageDescriptorLayout;
import com.destrostudios.icetea.core.material.descriptor.NormalMapDescriptor;
import com.destrostudios.icetea.core.material.descriptor.NormalMapDescriptorLayout;

import java.util.function.Function;

public class NormalMapComputeAction extends ComputeAction {

    public NormalMapComputeAction(Texture normalMapTexture, Texture dyTexture) {
        this.normalMapTexture = normalMapTexture;
        this.dyTexture = dyTexture;
    }
    private Texture normalMapTexture;
    private Texture dyTexture;

    @Override
    protected void fillMaterialDescriptorSet(Function<Integer, MaterialDescriptorLayout> getDescriptorLayout) {
        materialDescriptorSet.addDescriptor(new ComputeImageDescriptor("normalMap", (ComputeImageDescriptorLayout) getDescriptorLayout.apply(0), normalMapTexture, "rgba32f", true));
        materialDescriptorSet.addDescriptor(new NormalMapDescriptor("heightMap", (NormalMapDescriptorLayout) getDescriptorLayout.apply(1), dyTexture));
    }
}
