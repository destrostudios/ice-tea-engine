package com.destrostudios.icetea.core;

import com.destrostudios.icetea.core.object.NativeObject;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import static org.lwjgl.vulkan.VK10.*;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class Pipeline extends NativeObject {

    @Getter
    protected Long pipelineLayout;
    @Getter
    protected Long pipeline;

    @Override
    protected void cleanupNativeInternal() {
        vkDestroyPipeline(application.getLogicalDevice(), pipeline, null);
        pipeline = null;

        vkDestroyPipelineLayout(application.getLogicalDevice(), pipelineLayout, null);
        pipelineLayout = null;

        super.cleanupNativeInternal();
    }
}
