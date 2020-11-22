package com.destrostudios.icetea.core.materials.descriptors;

import com.destrostudios.icetea.core.Camera;

public class CameraTransformDescriptor extends UniformDescriptor<CameraTransformDescriptorLayout> {

    public CameraTransformDescriptor(String name, CameraTransformDescriptorLayout layout, Camera camera) {
        super(name, layout, camera.getTransformUniformData());
    }
}
