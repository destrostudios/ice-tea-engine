package com.destrostudios.icetea.core.lifecycle;

import com.destrostudios.icetea.core.Application;
import com.destrostudios.icetea.core.profiler.Profiler;

public class LifecycleObject {

    protected Application application;

    public final void update(Application application, int imageIndex, float tpf) {
        if (this.application == null) {
            this.application = application;
            executeProfiled(this::init, "init");
        }
        executeProfiled(() -> update(imageIndex, tpf), "update");
    }

    protected void init() {

    }

    protected void update(int imageIndex, float tpf) {

    }

    protected boolean isInitialized() {
        return (application != null);
    }

    public final void cleanup() {
        executeProfiled(this::cleanupInternal, "cleanup");
        application = null;
    }

    protected void cleanupInternal() {

    }

    private void executeProfiled(Runnable runnable, String method) {
        // Profiler might have to be fetched before or afterwards, because its reference can be not existing yet or already cleanuped up
        Profiler profiler = ((application != null) ? application.getProfiler() : null);
        long startNanoTime = System.nanoTime();
        runnable.run();
        long duration = (System.nanoTime() - startNanoTime);
        if (application != null) {
            profiler = application.getProfiler();
        }
        if (profiler != null) {
            profiler.addDuration(getClass().getName() + "." + method, duration);
            profiler.addDuration(getClass().getName() + "#" + hashCode() + "." + method, duration);
        }
    }
}
