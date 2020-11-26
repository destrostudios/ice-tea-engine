package com.destrostudios.icetea.core;

import lombok.Getter;
import lombok.Setter;
import org.lwjgl.system.MemoryStack;

public class Filter {

    public Filter() {
        uniformData = new UniformData();
    }
    protected Application application;
    @Getter
    protected Shader fragmentShader;
    @Getter
    protected UniformData uniformData;
    @Getter
    @Setter
    private boolean modified;

    public void init(Application application) {
        uniformData.setApplication(application);
        updateUniformData();
        uniformData.initBuffer();
    }

    public void updateUniformBuffers(int currentImage, MemoryStack stack) {
        updateUniformData();
        uniformData.updateBufferIfNecessary(currentImage, stack);
    }

    protected void updateUniformData() {

    }
}
