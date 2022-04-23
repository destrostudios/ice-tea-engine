package com.destrostudios.icetea.core.render.filter;

import com.destrostudios.icetea.core.filter.Filter;
import com.destrostudios.icetea.core.material.descriptor.SimpleTextureDescriptor;
import com.destrostudios.icetea.core.material.descriptor.SimpleTextureDescriptorLayout;
import com.destrostudios.icetea.core.material.descriptor.UniformDescriptor;
import com.destrostudios.icetea.core.material.descriptor.UniformDescriptorLayout;
import com.destrostudios.icetea.core.render.RenderJob;
import com.destrostudios.icetea.core.render.fullscreen.FullScreenQuadRenderJob;
import com.destrostudios.icetea.core.render.scene.SceneRenderJob;
import com.destrostudios.icetea.core.shader.Shader;

import static org.lwjgl.vulkan.VK10.VK_SHADER_STAGE_FRAGMENT_BIT;

public class FilterRenderJob extends FullScreenQuadRenderJob {

    public FilterRenderJob(Filter filter) {
        this.filter = filter;
    }
    private Filter filter;

    @Override
    protected void init() {
        // FullScreenQuadRenderJob needs the filter config material descriptor already in init
        filter.update(application, 0);
        super.init();
    }

    @Override
    public void update(float tpf) {
        super.update(tpf);
        filter.update(application, tpf);
    }

    @Override
    protected void fillMaterialDescriptorLayoutAndSet() {
        materialDescriptorSetLayout.addDescriptorLayout(new UniformDescriptorLayout(VK_SHADER_STAGE_FRAGMENT_BIT));
        materialDescriptorSet.addDescriptor(new UniformDescriptor("config", filter.getUniformData()));

        RenderJob<?> previousRenderJob = application.getSwapChain().getRenderJobManager().getPreviousRenderJob(this);
        SceneRenderJob sceneRenderJob = application.getSwapChain().getRenderJobManager().getSceneRenderJob();

        materialDescriptorSetLayout.addDescriptorLayout(new SimpleTextureDescriptorLayout());
        materialDescriptorSet.addDescriptor(new SimpleTextureDescriptor("colorMap", previousRenderJob.getResolvedColorTexture()));

        materialDescriptorSetLayout.addDescriptorLayout(new SimpleTextureDescriptorLayout());
        materialDescriptorSet.addDescriptor(new SimpleTextureDescriptor("depthMap", sceneRenderJob.getResolvedDepthTexture()));
    }

    @Override
    public Shader getFragmentShader() {
        return filter.getFragmentShader();
    }

    @Override
    protected void cleanupInternal() {
        filter.cleanup();
        super.cleanupInternal();
    }
}
