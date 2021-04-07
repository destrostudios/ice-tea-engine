package com.destrostudios.icetea.core.material.descriptor;

import com.destrostudios.icetea.core.scene.Geometry;

public class GeometryTransformDescriptor extends UniformDescriptor {

    public GeometryTransformDescriptor(String name, Geometry geometry) {
        super(name, geometry.getTransformUniformData());
    }
}
