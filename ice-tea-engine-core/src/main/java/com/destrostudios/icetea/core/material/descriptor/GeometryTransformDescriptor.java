package com.destrostudios.icetea.core.material.descriptor;

import com.destrostudios.icetea.core.scene.Geometry;

public class GeometryTransformDescriptor extends UniformDescriptor<GeometryTransformDescriptorLayout> {

    public GeometryTransformDescriptor(String name, GeometryTransformDescriptorLayout layout, Geometry geometry) {
        super(name, layout, geometry.getTransformUniformData());
    }
}
