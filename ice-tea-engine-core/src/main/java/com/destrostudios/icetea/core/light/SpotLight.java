package com.destrostudios.icetea.core.light;

import lombok.Getter;
import lombok.Setter;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public class SpotLight extends Light {

    public SpotLight() {
        translation = new Vector3f();
        rotation = new Quaternionf();
    }
    @Getter
    @Setter
    private Vector3f translation;
    @Getter
    @Setter
    private Quaternionf rotation;

    @Override
    protected void updateUniformDataFields() {
        super.updateUniformDataFields();
        uniformData.setVector3f("translation", translation);
    }
}
