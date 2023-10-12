package com.destrostudios.icetea.core.render.filter;

import com.destrostudios.icetea.core.filter.Filter;
import com.destrostudios.icetea.core.render.RenderJob;
import com.destrostudios.icetea.core.render.fullscreen.FullScreenQuadRenderJob;
import com.destrostudios.icetea.core.render.scene.SceneRenderJob;
import com.destrostudios.icetea.core.shader.Shader;

public class FilterRenderJob extends FullScreenQuadRenderJob {

    public FilterRenderJob(Filter filter) {
        this.filter = filter;
    }
    private Filter filter;

    @Override
    protected void initNative() {
        // FullScreenQuadRenderJob needs the filter config material descriptor already in init
        filter.updateNative(application);
        super.initNative();
    }

    @Override
    public void updateNative() {
        super.updateNative();
        filter.updateNative(application);
    }

    @Override
    protected void initResourceDescriptorSet() {
        super.initResourceDescriptorSet();
        resourceDescriptorSet.setDescriptor("config", filter.getUniformBuffer().getDescriptor("default"));

        RenderJob<?, ?> previousRenderJob = application.getSwapChain().getRenderJobManager().getPreviousRenderJob(this);
        SceneRenderJob sceneRenderJob = application.getSwapChain().getRenderJobManager().getSceneRenderJob();
        resourceDescriptorSet.setDescriptor("colorMap", previousRenderJob.getResolvedColorTexture().getDescriptor("default"));
        resourceDescriptorSet.setDescriptor("depthMap", sceneRenderJob.getResolvedDepthTexture().getDescriptor("default"));
    }

    @Override
    public Shader getFragmentShader() {
        return filter.getFragmentShader();
    }

    @Override
    protected void cleanupNativeInternal() {
        filter.cleanupNative();
        super.cleanupNativeInternal();
    }
}
