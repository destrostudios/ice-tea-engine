package com.destrostudios.icetea.core.render.filter;

import com.destrostudios.icetea.core.Application;
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
    public void init(Application application) {
        filter.init(application);
        super.init(application);
    }

    @Override
    public void updateUniformBuffers(int currentImage) {
        super.updateUniformBuffers(currentImage);
        filter.updateUniformBuffers(currentImage);
    }

    @Override
    protected void fillMaterialDescriptorLayoutAndSet() {
        UniformDescriptorLayout uniformDescriptorLayout = new UniformDescriptorLayout(VK_SHADER_STAGE_FRAGMENT_BIT);
        UniformDescriptor<UniformDescriptorLayout> uniformDescriptor = new UniformDescriptor<>("config", uniformDescriptorLayout, filter.getUniformData());
        materialDescriptorSetLayout.addDescriptorLayout(uniformDescriptorLayout);
        materialDescriptorSet.addDescriptor(uniformDescriptor);

        RenderJob<?> previousRenderJob = application.getSwapChain().getRenderJobManager().getPreviousRenderJob(this);
        SceneRenderJob sceneRenderJob = application.getSwapChain().getRenderJobManager().getSceneRenderJob();

        SimpleTextureDescriptorLayout colorTextureDescriptorLayout = new SimpleTextureDescriptorLayout();
        SimpleTextureDescriptor colorTextureDescriptor = new SimpleTextureDescriptor("colorMap", colorTextureDescriptorLayout, previousRenderJob.getResolvedColorTexture());
        materialDescriptorSetLayout.addDescriptorLayout(colorTextureDescriptorLayout);
        materialDescriptorSet.addDescriptor(colorTextureDescriptor);

        SimpleTextureDescriptorLayout depthTextureDescriptorLayout = new SimpleTextureDescriptorLayout();
        SimpleTextureDescriptor depthTextureDescriptor = new SimpleTextureDescriptor("depthMap", depthTextureDescriptorLayout, sceneRenderJob.getResolvedDepthTexture());
        materialDescriptorSetLayout.addDescriptorLayout(depthTextureDescriptorLayout);
        materialDescriptorSet.addDescriptor(depthTextureDescriptor);
    }

    @Override
    public Shader getFragmentShader() {
        return filter.getFragmentShader();
    }
}
