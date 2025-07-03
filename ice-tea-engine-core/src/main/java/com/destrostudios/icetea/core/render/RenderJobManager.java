package com.destrostudios.icetea.core.render;

import com.destrostudios.icetea.core.object.NativeObject;
import com.destrostudios.icetea.core.render.filter.FilterRenderJob;
import com.destrostudios.icetea.core.render.scene.SceneRenderJob;
import lombok.Getter;

import java.util.Collection;
import java.util.LinkedList;
import java.util.function.Consumer;

public class RenderJobManager extends NativeObject {

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
    private boolean modified;

    @Override
    public void updateNative() {
        super.updateNative();
        if (modified) {
            cleanupRenderJobsNative();
            modified = false;
        }
        forEachRenderJob(renderJob -> renderJob.updateNative(application));
        forEachRenderJob(RenderJob::afterAllRenderJobsUpdatedNative);
    }

    public RenderJob<?> getPresentingRenderJob() {
        return ((queuePostScene.size() > 0) ? queuePostScene.getLast() : sceneRenderJob);
    }

    public RenderJob<?> getPreviousRenderJob(FilterRenderJob filterRenderJob) {
        int postSceneIndex = queuePostScene.indexOf(filterRenderJob);
        return ((postSceneIndex > 0) ? queuePostScene.get(postSceneIndex - 1) : sceneRenderJob);
    }

    @Override
    protected void cleanupNativeInternal() {
        cleanupRenderJobsNative();
        super.cleanupNativeInternal();
    }

    private void cleanupRenderJobsNative() {
        forEachRenderJob(RenderJob::cleanupNative);
    }

    public void forEachRenderJob(Consumer<RenderJob<?>> renderJobConsumer) {
        queuePreScene.forEach(renderJobConsumer);
        renderJobConsumer.accept(sceneRenderJob);
        queuePostScene.forEach(renderJobConsumer);
    }

    // Add & Remove

    public void addPreSceneRenderJob(RenderJob<?> renderJob) {
        queuePreScene.add(renderJob);
        modified = true;
    }

    public void addPreSceneRenderJobs(Collection<? extends RenderJob<?>> renderJobs) {
        queuePreScene.addAll(renderJobs);
        modified = true;
    }

    public void removePreSceneRenderJob(RenderJob<?> renderJob) {
        if (queuePreScene.remove(renderJob)) {
            modified = true;
        }
    }

    public void removePreSceneRenderJobs(Collection<? extends RenderJob<?>> renderJobs) {
        if (queuePreScene.removeAll(renderJobs)) {
            modified = true;
        }
    }

    public void addPostSceneRenderJob(RenderJob<?> renderJob) {
        queuePostScene.add(renderJob);
        modified = true;
    }

    public void addPostSceneRenderJobs(Collection<? extends RenderJob<?>> renderJobs) {
        queuePostScene.addAll(renderJobs);
        modified = true;
    }

    public void removePostSceneRenderJob(RenderJob<?> renderJob) {
        if (queuePostScene.remove(renderJob)) {
            modified = true;
        }
    }

    public void removePostSceneRenderJobs(Collection<? extends RenderJob<?>> renderJobs) {
        if (queuePostScene.removeAll(renderJobs)) {
            modified = true;
        }
    }
}
