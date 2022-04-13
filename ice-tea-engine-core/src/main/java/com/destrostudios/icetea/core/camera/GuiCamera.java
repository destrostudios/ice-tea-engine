package com.destrostudios.icetea.core.camera;

public class GuiCamera extends Camera {

    public void setWindowSize(int width, int height) {
        viewMatrix.set(
            (2f / width), 0, 0, 0,
            0, (2f / height), 0, 0,
            0, 0, 1, 0,
            -1, -1, 0, 1
        );
        updateProjectionViewMatrix();
        updateUniform_ViewMatrix();
    }
}
