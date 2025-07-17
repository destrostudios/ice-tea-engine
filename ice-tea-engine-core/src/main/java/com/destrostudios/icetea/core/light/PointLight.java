package com.destrostudios.icetea.core.light;

import lombok.Getter;
import lombok.Setter;
import org.joml.Vector3f;

public class PointLight extends Light {

    public PointLight() {
        position = new Vector3f();
    }
    @Getter
    @Setter
    private Vector3f position;

    @Override
    protected void updateUniformBufferFields() {
        super.updateUniformBufferFields();
        uniformBuffer.getData().setVector3f("position", position);
    }
}
