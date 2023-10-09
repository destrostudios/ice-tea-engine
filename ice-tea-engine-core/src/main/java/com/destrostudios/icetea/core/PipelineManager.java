package com.destrostudios.icetea.core;

import com.destrostudios.icetea.core.object.NativeObject;

import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

public class PipelineManager extends NativeObject {

    // TODO: Add cache control like a max size
    private ConcurrentHashMap<Object, Pipeline> pipelines = new ConcurrentHashMap<>();

    public <T> Pipeline getOrCreate(T state, Function<T, Pipeline> createPipeline) {
        return pipelines.computeIfAbsent(state, s -> createPipeline.apply((T) s));
    }

    @Override
    protected void cleanupNativeInternal() {
        for (Pipeline pipeline : pipelines.values()) {
            pipeline.cleanupNative();
        }
        super.cleanupNativeInternal();
    }
}
