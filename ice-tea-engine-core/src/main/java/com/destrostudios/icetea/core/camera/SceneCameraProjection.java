package com.destrostudios.icetea.core.camera;

import org.joml.Matrix4f;

public abstract class SceneCameraProjection implements Cloneable {

    public abstract void updateProjectionMatrix(Matrix4f projectionMatrix);

    public abstract SceneCameraProjection clone();
}
