package com.destrostudios.icetea.core.filter;

import com.destrostudios.icetea.core.Application;
import com.destrostudios.icetea.core.data.UniformData;
import com.destrostudios.icetea.core.lifecycle.LifecycleObject;
import com.destrostudios.icetea.core.render.filter.FilterRenderJob;
import com.destrostudios.icetea.core.shader.Shader;
import lombok.Getter;

public class Filter extends LifecycleObject {

    public Filter() {
        uniformData = new UniformData();
        filterRenderJob = new FilterRenderJob(this);
    }
    @Getter
    protected Shader fragmentShader;
    @Getter
    protected UniformData uniformData;
    @Getter
    private FilterRenderJob filterRenderJob;

    @Override
    public void update(Application application, int imageIndex, float tpf) {
        super.update(application, imageIndex, tpf);
        updateUniformData();
        uniformData.updateBufferAndCheckRecreation(application, imageIndex, tpf, application.getSwapChain().getImages().size());
    }

    protected void updateUniformData() {

    }

    @Override
    public void cleanup() {
        uniformData.cleanup();
        filterRenderJob.cleanup();
        super.cleanup();
    }
}
