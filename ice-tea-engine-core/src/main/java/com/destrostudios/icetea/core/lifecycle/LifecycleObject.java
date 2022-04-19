package com.destrostudios.icetea.core.lifecycle;

import com.destrostudios.icetea.core.Application;
import com.destrostudios.icetea.core.profiler.Profiler;

import java.util.function.BiConsumer;

public class LifecycleObject {

    protected Application application;

    public final void update(Application application, int imageIndex, float tpf) {
        if (this.application == null) {
            this.application = application;
            executeProfiled(this::init, (profiler, duration) -> profiler.onInit(this, duration));
        }
        executeProfiled(() -> update(imageIndex, tpf), (profiler, duration) -> profiler.onUpdate(this, duration));
    }

    protected void init() {

    }

    protected void update(int imageIndex, float tpf) {

    }

    protected boolean isInitialized() {
        return (application != null);
    }

    public final void cleanup() {
        executeProfiled(this::cleanupInternal, (profiler, duration) -> profiler.onCleanup(this, duration));
        application = null;
    }

    protected void cleanupInternal() {

    }

    private void executeProfiled(Runnable runnable, BiConsumer<Profiler, Long> profileDuration) {
        // Profiler might have to be fetched before or afterwards, because its reference can be not existing yet or already cleanuped up
        Profiler profiler = ((application != null) ? application.getProfiler() : null);
        long startNanoTime = System.nanoTime();
        runnable.run();
        long duration = (System.nanoTime() - startNanoTime);
        if (application != null) {
            profiler = application.getProfiler();
        }
        if (profiler != null) {
            profileDuration.accept(profiler, duration);
        }
    }
}
