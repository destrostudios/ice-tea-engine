package com.destrostudios.icetea.samples.water;

import com.destrostudios.icetea.core.compute.ComputeAction;
import com.destrostudios.icetea.core.data.StorageBufferData;
import com.destrostudios.icetea.core.data.UniformData;
import com.destrostudios.icetea.core.material.descriptor.ComputeImageDescriptor;
import com.destrostudios.icetea.core.material.descriptor.StorageBufferDescriptor;
import com.destrostudios.icetea.core.material.descriptor.UniformDescriptor;
import com.destrostudios.icetea.core.texture.Texture;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class TwiddleFactorsComputeAction extends ComputeAction {

    private Texture twiddleFactorsTexture;
    private StorageBufferData storageBufferData;
    private UniformData uniformData;

    @Override
    protected void fillMaterialDescriptorSet() {
        materialDescriptorSet.addDescriptor(new ComputeImageDescriptor("twiddleIndices", twiddleFactorsTexture, "rgba32f", true));
        materialDescriptorSet.addDescriptor(new StorageBufferDescriptor("myBuffer", storageBufferData));
        materialDescriptorSet.addDescriptor(new UniformDescriptor("uniforms", uniformData));
    }
}
