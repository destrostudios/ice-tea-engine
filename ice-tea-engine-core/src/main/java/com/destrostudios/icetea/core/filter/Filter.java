package com.destrostudios.icetea.core.filter;

import com.destrostudios.icetea.core.buffer.UniformDataBuffer;
import com.destrostudios.icetea.core.lifecycle.LifecycleObject;
import com.destrostudios.icetea.core.resource.descriptor.UniformDescriptor;
import com.destrostudios.icetea.core.render.filter.FilterRenderJob;
import com.destrostudios.icetea.core.shader.Shader;
import lombok.Getter;

import static org.lwjgl.vulkan.VK10.VK_SHADER_STAGE_FRAGMENT_BIT;

public class Filter extends LifecycleObject {

    public Filter() {
        uniformBuffer = new UniformDataBuffer();
        uniformBuffer.setDescriptor("default", new UniformDescriptor(VK_SHADER_STAGE_FRAGMENT_BIT));
        filterRenderJob = new FilterRenderJob(this);
    }
    @Getter
    protected Shader fragmentShader;
    @Getter
    protected UniformDataBuffer uniformBuffer;
    @Getter
    private FilterRenderJob filterRenderJob;

    @Override
    public void update(float tpf) {
        super.update(tpf);
        updateUniformBuffer();
        application.getSwapChain().setResourceActive(uniformBuffer);
    }

    protected void updateUniformBuffer() {

    }

    @Override
    protected void cleanupInternal() {
        uniformBuffer.cleanup();
        super.cleanupInternal();
    }
}
