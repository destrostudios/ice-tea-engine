package com.destrostudios.icetea.core.animation;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class AnimationSamplerData<T> {
    private float[] keyframeTimes;
    private T[] keyframeValues;
}
