package com.destrostudios.icetea.core.lights;

import com.destrostudios.icetea.core.Light;
import lombok.Getter;
import lombok.Setter;
import org.joml.Vector3f;

public class SpotLight extends Light {

    public SpotLight() {
        translation = new Vector3f();
        rotation = new Vector3f();
    }
    @Getter
    @Setter
    private Vector3f translation;
    @Getter
    @Setter
    private Vector3f rotation;

    @Override
    protected void updateUniformDataFields() {
        super.updateUniformDataFields();
        uniformData.setVector3f("translation", translation);
        uniformData.setVector3f("rotation", rotation);
    }
}
