package com.destrostudios.icetea.core.filter;

import com.destrostudios.icetea.core.Application;
import com.destrostudios.icetea.core.data.UniformData;
import com.destrostudios.icetea.core.render.filter.FilterRenderJob;
import com.destrostudios.icetea.core.shader.Shader;
import lombok.Getter;

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
        uniformData.initBuffers(application.getSwapChain().getImages().size());
    }

    public void updateUniformBuffers(int currentImage) {
        updateUniformData();
        uniformData.updateBufferIfNecessary(currentImage);
    }

    protected void updateUniformData() {

    }

    public void cleanup() {
        uniformData.cleanupBuffer();
        filterRenderJob.cleanup();
    }
}
