package com.destrostudios.icetea.core.render;

import com.destrostudios.icetea.core.material.descriptor.*;
import com.destrostudios.icetea.core.texture.Texture;

import java.util.Map;
import java.util.function.Supplier;

public abstract class EssentialGeometryRenderContext<RJ extends RenderJob<?>> extends GeometryRenderContext<RJ> {

    @Override
    protected void fillMaterialDescriptorSet(MaterialDescriptorSetLayout descriptorSetLayout, MaterialDescriptorSet descriptorSet) {
        descriptorSetLayout.addDescriptorLayout(new GeometryTransformDescriptorLayout());
        descriptorSet.addDescriptor(new GeometryTransformDescriptor("geometry", geometry));

        for (MaterialDescriptorWithLayout descriptorWithLayout : geometry.getAdditionalMaterialDescriptors()) {
            descriptorSetLayout.addDescriptorLayout(descriptorWithLayout.getLayout());
            descriptorSet.addDescriptor(descriptorWithLayout.getDescriptor());
        }

        if (geometry.getMaterial().getParameters().getSize() > 0) {
            descriptorSetLayout.addDescriptorLayout(new MaterialParamsDescriptorLayout());
            descriptorSet.addDescriptor(new MaterialParamsDescriptor("params", geometry.getMaterial()));
        }

        for (Map.Entry<String, Supplier<Texture>> entry : geometry.getMaterial().getTextureSuppliers().entrySet()) {
            descriptorSetLayout.addDescriptorLayout(new SimpleTextureDescriptorLayout());
            descriptorSet.addDescriptor(new SimpleTextureDescriptor(entry.getKey(), entry.getValue().get()));
        }
    }
}
