package com.destrostudios.icetea.core.material.descriptor;

import com.destrostudios.icetea.core.model.Skeleton;

public class SkeletonDescriptor extends UniformDescriptor {

    public SkeletonDescriptor(String name, Skeleton skeleton) {
        super(name, skeleton.getUniformData());
    }
}
