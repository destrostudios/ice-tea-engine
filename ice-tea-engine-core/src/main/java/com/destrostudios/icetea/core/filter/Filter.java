package com.destrostudios.icetea.core.filter;

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
    public void update(float tpf) {
        super.update(tpf);
        updateUniformData();
        uniformData.update(application, tpf);
    }

    protected void updateUniformData() {

    }

    @Override
    protected void cleanupInternal() {
        uniformData.cleanup();
        super.cleanupInternal();
    }
}
