package com.destrostudios.icetea.core.filter;

import com.destrostudios.icetea.core.buffer.UniformDataBuffer;
import com.destrostudios.icetea.core.object.NativeObject;
import com.destrostudios.icetea.core.resource.descriptor.UniformDescriptor;
import com.destrostudios.icetea.core.render.filter.FilterRenderJob;
import com.destrostudios.icetea.core.shader.Shader;
import lombok.Getter;

import static org.lwjgl.vulkan.VK10.VK_SHADER_STAGE_FRAGMENT_BIT;

public class Filter extends NativeObject {

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
    protected void updateNative() {
        super.updateNative();
        updateUniformBuffer();
        uniformBuffer.updateNative(application);
    }

    protected void updateUniformBuffer() {

    }

    @Override
    protected void cleanupNativeInternal() {
        uniformBuffer.cleanupNative();
        super.cleanupNativeInternal();
    }
}
