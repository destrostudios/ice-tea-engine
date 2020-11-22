package com.destrostudios.icetea.core.materials.descriptors;

import com.destrostudios.icetea.core.Geometry;

public class GeometryTransformDescriptor extends UniformDescriptor<GeometryTransformDescriptorLayout> {

    public GeometryTransformDescriptor(String name, GeometryTransformDescriptorLayout layout, Geometry geometry) {
        super(name, layout, geometry.getTransformUniformData());
    }
}
