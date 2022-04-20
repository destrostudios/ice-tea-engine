package com.destrostudios.icetea.core.profiler;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

public class Profiler {

    private HashMap<String, ProfilerDurations> durations = new HashMap<>();

    public void clear() {
        durations.clear();
    }

    public void addDuration(String key, long duration) {
        ProfilerDurations profilerDurations = durations.computeIfAbsent(key, ProfilerDurations::new);
        profilerDurations.addDuration(duration);
    }

    public List<ProfilerDurations> getSortedResults(ProfilerOrder order) {
        return durations.values().stream()
                .sorted(Comparator.comparingDouble(pd -> -1 * getOrderValue(pd, order)))
                .collect(Collectors.toList());
    }

    private double getOrderValue(ProfilerDurations profilerDurations, ProfilerOrder order) {
        switch (order) {
            case INVOCATIONS: return profilerDurations.getInvocations();
            case MINIMUM_DURATION: return profilerDurations.getMinimumDuration();
            case MAXIMUM_DURATION: return profilerDurations.getMaximumDuration();
        }
        return profilerDurations.getAverageDuration();
    }
}
