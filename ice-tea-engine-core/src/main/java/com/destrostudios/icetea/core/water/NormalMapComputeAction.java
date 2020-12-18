package com.destrostudios.icetea.core.water;

import com.destrostudios.icetea.core.ComputeAction;
import com.destrostudios.icetea.core.MaterialDescriptorLayout;
import com.destrostudios.icetea.core.Texture;
import com.destrostudios.icetea.core.materials.descriptors.ComputeImageDescriptor;
import com.destrostudios.icetea.core.materials.descriptors.ComputeImageDescriptorLayout;
import com.destrostudios.icetea.core.materials.descriptors.NormalMapDescriptor;
import com.destrostudios.icetea.core.materials.descriptors.NormalMapDescriptorLayout;

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
