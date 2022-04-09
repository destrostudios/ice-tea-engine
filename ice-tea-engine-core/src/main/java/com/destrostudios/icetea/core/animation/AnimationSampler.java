package com.destrostudios.icetea.core.animation;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public abstract class AnimationSampler<T> {

    private AnimationSamplerData<T> data;

    public T getValue(float time) {
        T[] keyframeValues = data.getKeyframeValues();
        if (keyframeValues.length == 1) {
            return keyframeValues[0];
        }
        float[] keyframeTimes = data.getKeyframeTimes();
        time %= keyframeTimes[keyframeTimes.length - 1];
        int keyframeIndex1 = -1;
        int keyframeIndex2 = -1;
        time %= keyframeTimes[keyframeTimes.length - 1];
        for (int i = 1; i < keyframeTimes.length; i++) {
            if (keyframeTimes[i] >= time) {
                keyframeIndex1 = i - 1;
                keyframeIndex2 = i;
                break;
            }
        }
        if (keyframeIndex1 == -1) {
            return null;
        }
        float stepDuration = (keyframeTimes[keyframeIndex2] - keyframeTimes[keyframeIndex1]);
        // TODO: Support all possible interpolations (not only linear)
        float progress = ((time - keyframeTimes[keyframeIndex1]) / stepDuration);
        return interpolate(keyframeValues[keyframeIndex1], keyframeValues[keyframeIndex2], progress);
    }

    protected abstract T interpolate(T value1, T value2, float progress);

    // TODO: Support loop modes
    public boolean isFinished(float time) {
        return false; // (time > keyframeTimes[keyframeTimes.length - 1]);
    }
}
