package com.destrostudios.icetea.core.material.descriptor;

import com.destrostudios.icetea.core.camera.Camera;

public class CameraTransformDescriptor extends UniformDescriptor {

    public CameraTransformDescriptor(String name, Camera camera) {
        super(name, camera.getTransformUniformData());
    }
}
