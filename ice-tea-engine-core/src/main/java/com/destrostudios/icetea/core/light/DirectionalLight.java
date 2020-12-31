package com.destrostudios.icetea.core.light;

import lombok.Getter;
import lombok.Setter;
import org.joml.Vector3f;

public class DirectionalLight extends Light {

    public DirectionalLight() {
        direction = new Vector3f();
    }
    @Getter
    @Setter
    private Vector3f direction;

    @Override
    protected void updateUniformDataFields() {
        super.updateUniformDataFields();
        uniformData.setVector3f("direction", direction);
    }
}
