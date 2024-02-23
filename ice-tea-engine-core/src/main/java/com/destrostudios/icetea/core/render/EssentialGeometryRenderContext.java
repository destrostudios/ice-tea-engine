package com.destrostudios.icetea.core.render;

import com.destrostudios.icetea.core.resource.ResourceDescriptor;
import com.destrostudios.icetea.core.resource.ResourceReusability;
import com.destrostudios.icetea.core.scene.Geometry;
import com.destrostudios.icetea.core.texture.Texture;

import java.util.HashMap;
import java.util.Map;

public class EssentialGeometryRenderContext<RJ extends RenderJob<?, ?>> extends GeometryRenderContext<RJ> {

    public EssentialGeometryRenderContext(Geometry geometry, RJ renderJob) {
        super(geometry, renderJob);
    }
    // TODO: Introduce TempVars
    private HashMap<String, ResourceDescriptor<?>> tmpAdditionalResourceDescriptors = new HashMap<>();

    @Override
    protected void setDescriptors() {
        resourceDescriptorSet.setDescriptor("geometry", geometry.getTransformUniformBuffer().getDescriptor("default"), ResourceReusability.LOW);
        tmpAdditionalResourceDescriptors.clear();
        geometry.addAdditionalResourceDescriptors(tmpAdditionalResourceDescriptors);
        for (Map.Entry<String, ResourceDescriptor<?>> entry : tmpAdditionalResourceDescriptors.entrySet()) {
            resourceDescriptorSet.setDescriptor(entry.getKey(), entry.getValue(), ResourceReusability.LOW);
        }
        if (geometry.getMaterial().getParameters().getSize() > 0) {
            resourceDescriptorSet.setDescriptor("params", geometry.getMaterial().getParametersBuffer().getDescriptor("default"));
        }
        for (Map.Entry<String, Texture> entry : geometry.getMaterial().getTextures().entrySet()) {
            resourceDescriptorSet.setDescriptor(entry.getKey(), entry.getValue().getDescriptor("default"));
        }
    }
}
