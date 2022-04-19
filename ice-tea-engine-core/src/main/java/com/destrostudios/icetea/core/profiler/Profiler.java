package com.destrostudios.icetea.core.profiler;

import com.destrostudios.icetea.core.lifecycle.LifecycleObject;

import java.util.HashMap;

public class Profiler {

    private HashMap<LifecycleObject, ProfilerDurations> init = new HashMap<>();
    private HashMap<LifecycleObject, ProfilerDurations> update = new HashMap<>();
    private HashMap<LifecycleObject, ProfilerDurations> cleanup = new HashMap<>();

    public void clear() {
        init.clear();
        update.clear();
        cleanup.clear();
    }

    public void onInit(LifecycleObject lifecycleObject, long duration) {
        getDurations(init, lifecycleObject).addDuration(duration);
    }

    public void onUpdate(LifecycleObject lifecycleObject, long duration) {
        getDurations(update, lifecycleObject).addDuration(duration);
    }

    public void onCleanup(LifecycleObject lifecycleObject, long duration) {
        getDurations(cleanup, lifecycleObject).addDuration(duration);
    }

    private ProfilerDurations getDurations(HashMap<LifecycleObject, ProfilerDurations> durations, LifecycleObject lifecycleObject) {
        return durations.computeIfAbsent(lifecycleObject, lo -> new ProfilerDurations());
    }
}
