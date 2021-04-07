package com.destrostudios.icetea.core.model;

import com.destrostudios.icetea.core.material.descriptor.MaterialDescriptorWithLayout;
import com.destrostudios.icetea.core.material.descriptor.SkeletonDescriptor;
import com.destrostudios.icetea.core.material.descriptor.SkeletonDescriptorLayout;
import com.destrostudios.icetea.core.scene.Control;
import com.destrostudios.icetea.core.scene.Geometry;
import lombok.Getter;

public class SkeletonGeometryControl extends Control {

    public SkeletonGeometryControl(Skeleton skeleton) {
        this.skeleton = skeleton;
        skeletonDescriptor = new MaterialDescriptorWithLayout(new SkeletonDescriptorLayout(), new SkeletonDescriptor("skeleton", skeleton));
    }
    @Getter
    private Skeleton skeleton;
    private MaterialDescriptorWithLayout skeletonDescriptor;

    @Override
    protected void onAdd() {
        super.onAdd();
        Geometry geometry = (Geometry) spatial;
        geometry.addAdditionalMaterialDescriptor(skeletonDescriptor);
    }

    @Override
    public void onRemove() {
        Geometry geometry = (Geometry) spatial;
        geometry.removeAdditionalMaterialDescriptor(skeletonDescriptor);
        super.onRemove();
    }
}
