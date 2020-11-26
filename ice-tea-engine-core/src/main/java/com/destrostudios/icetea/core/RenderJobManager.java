package com.destrostudios.icetea.core;

import lombok.Getter;

import java.util.LinkedList;
import java.util.function.Consumer;

public class RenderJobManager {

    public RenderJobManager() {
        queuePreScene = new LinkedList<>();
        sceneRenderJob = new SceneRenderJob();
        queuePostScene = new LinkedList<>();
    }
    @Getter
    private LinkedList<RenderJob<?>> queuePreScene;
    @Getter
    private SceneRenderJob sceneRenderJob;
    @Getter
    private LinkedList<RenderJob<?>> queuePostScene;

    public void forEachRenderJob(Consumer<RenderJob<?>> renderJobConsumer) {
        queuePreScene.forEach(renderJobConsumer);
        renderJobConsumer.accept(sceneRenderJob);
        queuePostScene.forEach(renderJobConsumer);
    }

    public RenderJob<?> getPresentingRenderJob() {
        return ((queuePostScene.size() > 0) ? queuePostScene.getLast() : sceneRenderJob);
    }

    public RenderJob<?> getPreviousRenderJob(FilterRenderJob filterRenderJob) {
        int postSceneIndex = queuePostScene.indexOf(filterRenderJob);
        return ((postSceneIndex > 0) ? queuePostScene.get(postSceneIndex - 1) : sceneRenderJob);
    }
}
