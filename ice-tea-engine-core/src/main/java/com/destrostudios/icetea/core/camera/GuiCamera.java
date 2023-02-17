package com.destrostudios.icetea.core.camera;

import com.destrostudios.icetea.core.WindowResizeListener;

public class GuiCamera extends Camera implements WindowResizeListener {

    @Override
    protected void init() {
        super.init();
        application.addWindowResizeListener(this);
        onWindowResize(application.getConfig().getWidth(), application.getConfig().getHeight());
    }

    @Override
    public void onWindowResize(int width, int height) {
        viewMatrix.set(
            (2f / width), 0, 0, 0,
            0, (2f / height), 0, 0,
            0, 0, 1, 0,
            -1, -1, 0, 1
        );
        updateProjectionViewMatrix();
        updateUniform_ViewMatrix();
    }

    @Override
    protected void cleanupInternal() {
        application.removeWindowResizeListener(this);
        super.cleanupInternal();
    }
}
