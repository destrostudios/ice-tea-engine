package com.destrostudios.icetea.core;

import lombok.Getter;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

public class RenderJobManager {

    public RenderJobManager() {
        bucketPreScene = new HashSet<>();
        bucketScene = new HashSet<>();
        bucketPostScene = new HashSet<>();
    }
    @Getter
    private Set<RenderJob<?>> bucketPreScene;
    @Getter
    private Set<RenderJob<?>> bucketScene;
    @Getter
    private Set<RenderJob<?>> bucketPostScene;

    public void forEachRenderJob(Consumer<RenderJob<?>> renderJobConsumer) {
        bucketPreScene.forEach(renderJobConsumer);
        bucketScene.forEach(renderJobConsumer);
        bucketPostScene.forEach(renderJobConsumer);
    }
}
