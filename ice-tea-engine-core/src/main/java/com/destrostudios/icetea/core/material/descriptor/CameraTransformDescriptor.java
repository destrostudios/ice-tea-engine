package com.destrostudios.icetea.core.material.descriptor;

import com.destrostudios.icetea.core.scene.Camera;

public class CameraTransformDescriptor extends UniformDescriptor<CameraTransformDescriptorLayout> {

    public CameraTransformDescriptor(String name, CameraTransformDescriptorLayout layout, Camera camera) {
        super(name, layout, camera.getTransformUniformData());
    }
}
