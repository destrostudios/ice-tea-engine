package com.destrostudios.icetea.core.animation.samplers;

import com.destrostudios.icetea.core.animation.AnimationSampler;
import com.destrostudios.icetea.core.animation.AnimationSamplerData;
import org.joml.Vector3f;

public class Vector3fAnimationSampler extends AnimationSampler<Vector3f> {

    public Vector3fAnimationSampler(AnimationSamplerData<Vector3f> samplerData) {
        super(samplerData);
    }

    @Override
    protected Vector3f interpolate(Vector3f value1, Vector3f value2, float progress) {
        // TODO: Introduce TempVars
        return value1.add(value2.sub(value1, new Vector3f()).mul(progress), new Vector3f());
    }
}
