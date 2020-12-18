package com.destrostudios.icetea.core.water;

import com.destrostudios.icetea.core.*;
import com.destrostudios.icetea.core.materials.descriptors.*;

import java.util.function.Function;

public class TwiddleFactorsComputeAction extends ComputeAction {

    public TwiddleFactorsComputeAction(Texture twiddleFactorsTexture, StorageBufferData storageBufferData, UniformData uniformData) {
        this.twiddleFactorsTexture = twiddleFactorsTexture;
        this.storageBufferData = storageBufferData;
        this.uniformData = uniformData;
    }
    private Texture twiddleFactorsTexture;
    private StorageBufferData storageBufferData;
    private UniformData uniformData;

    @Override
    protected void fillMaterialDescriptorSet(Function<Integer, MaterialDescriptorLayout> getDescriptorLayout) {
        materialDescriptorSet.addDescriptor(new ComputeImageDescriptor("twiddleIndices", (ComputeImageDescriptorLayout) getDescriptorLayout.apply(0), twiddleFactorsTexture, "rgba32f", true));
        materialDescriptorSet.addDescriptor(new StorageBufferDescriptor("myBuffer", (StorageBufferDescriptorLayout) getDescriptorLayout.apply(1), storageBufferData));
        materialDescriptorSet.addDescriptor(new UniformDescriptor<>("uniforms", (UniformDescriptorLayout) getDescriptorLayout.apply(2), uniformData));
    }
}
