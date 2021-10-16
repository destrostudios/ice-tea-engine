package com.destrostudios.icetea.core.animation.samplers;

import com.destrostudios.icetea.core.animation.AnimationSampler;
import com.destrostudios.icetea.core.animation.AnimationSamplerData;
import org.joml.Quaternionf;

public class QuaternionAnimationSampler extends AnimationSampler<Quaternionf> {

    public QuaternionAnimationSampler(AnimationSamplerData<Quaternionf> samplerData) {
        super(samplerData);
    }

    @Override
    protected Quaternionf interpolate(Quaternionf value1, Quaternionf value2, float progress) {
        // TODO: Introduce TempVars
        return value1.slerp(value2, progress, new Quaternionf());
    }
}
