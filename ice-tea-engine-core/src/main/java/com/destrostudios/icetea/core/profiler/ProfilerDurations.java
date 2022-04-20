package com.destrostudios.icetea.core.profiler;

import lombok.Getter;

public class ProfilerDurations {

    public ProfilerDurations(String key) {
        this.key = key;
    }
    @Getter
    private String key;
    @Getter
    private long invocations;
    private long durationsSum;
    @Getter
    private long minimumDuration = Long.MAX_VALUE;
    @Getter
    private long maximumDuration = Long.MIN_VALUE;

    public void addDuration(long duration) {
        invocations++;
        durationsSum += duration;
        if (duration < minimumDuration) {
            minimumDuration = duration;
        }
        if (duration > maximumDuration) {
            maximumDuration = duration;
        }
    }

    public long getAverageDuration() {
        return (durationsSum / invocations);
    }
}
