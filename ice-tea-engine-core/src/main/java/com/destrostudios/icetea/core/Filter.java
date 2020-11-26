package com.destrostudios.icetea.core;

import lombok.Getter;
import org.lwjgl.system.MemoryStack;

public class Filter {

    public Filter() {
        uniformData = new UniformData();
        filterRenderJob = new FilterRenderJob(this);
    }
    protected Application application;
    @Getter
    protected Shader fragmentShader;
    @Getter
    protected UniformData uniformData;
    @Getter
    private FilterRenderJob filterRenderJob;

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

    public void cleanup() {
        uniformData.cleanupBuffer();
        filterRenderJob.cleanup();
    }
}
