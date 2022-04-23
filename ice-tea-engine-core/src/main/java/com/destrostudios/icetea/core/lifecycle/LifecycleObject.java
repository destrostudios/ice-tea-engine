package com.destrostudios.icetea.core.lifecycle;

import com.destrostudios.icetea.core.Application;
import com.destrostudios.icetea.core.profiler.Profiler;

public class LifecycleObject {

    protected Application application;

    public final void update(Application application, float tpf) {
        if (this.application == null) {
            this.application = application;
            executeProfiled(this::init, "init");
        }
        executeProfiled(() -> update(tpf), "update");
    }

    protected void init() {

    }

    protected void update(float tpf) {

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
        // Application might have to be fetched before or afterwards, because its reference can be not existing yet or already cleaned up
        Application tmpApplication = application;
        long startNanoTime = System.nanoTime();
        runnable.run();
        long duration = (System.nanoTime() - startNanoTime);
        if (application != null) {
            tmpApplication = application;
        }
        if ((tmpApplication != null) && tmpApplication.getConfig().isEnableProfiler()) {
            Profiler profiler = application.getProfiler();
            profiler.addDuration(getClass().getName() + "." + method, duration);
            profiler.addDuration(getClass().getName() + "#" + hashCode() + "." + method, duration);
        }
    }
}
