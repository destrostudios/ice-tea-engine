package com.destrostudios.icetea.core.render;

import com.destrostudios.icetea.core.resource.ResourceDescriptor;
import com.destrostudios.icetea.core.scene.Geometry;
import com.destrostudios.icetea.core.texture.Texture;

import java.util.HashMap;
import java.util.Map;

public abstract class EssentialGeometryRenderContext<RJ extends RenderJob<?>> extends GeometryRenderContext<RJ> {

    public EssentialGeometryRenderContext(Geometry geometry, RJ renderJob) {
        super(geometry, renderJob);
    }
    // TODO: Introduce TempVars
    private HashMap<String, ResourceDescriptor<?>> tmpAdditionalResourceDescriptors = new HashMap<>();

    @Override
    protected void initNative() {
        super.initNative();
        resourceDescriptorSet.setDescriptor("geometry", geometry.getTransformUniformBuffer().getDescriptor("default"));
        tmpAdditionalResourceDescriptors.clear();
        geometry.addAdditionalResourceDescriptors(tmpAdditionalResourceDescriptors);
        for (Map.Entry<String, ResourceDescriptor<?>> entry : tmpAdditionalResourceDescriptors.entrySet()) {
            resourceDescriptorSet.setDescriptor(entry.getKey(), entry.getValue());
        }
        if (geometry.getMaterial().getParameters().getSize() > 0) {
            resourceDescriptorSet.setDescriptor("params", geometry.getMaterial().getParametersBuffer().getDescriptor("default"));
        }
        for (Map.Entry<String, Texture> entry : geometry.getMaterial().getTextures().entrySet()) {
            resourceDescriptorSet.setDescriptor(entry.getKey(), entry.getValue().getDescriptor("default"));
        }
    }
}
