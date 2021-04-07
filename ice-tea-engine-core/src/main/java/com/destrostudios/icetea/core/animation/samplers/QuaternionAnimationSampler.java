package com.destrostudios.icetea.core.animation.samplers;

import com.destrostudios.icetea.core.animation.AnimationSampler;
import com.destrostudios.icetea.core.animation.AnimationSamplerData;
import org.joml.Quaternionf;
import org.joml.Vector4f;

public class QuaternionAnimationSampler extends AnimationSampler<Quaternionf> {

    public QuaternionAnimationSampler(AnimationSamplerData<Quaternionf> samplerData) {
        super(samplerData);
    }

    @Override
    protected Quaternionf interpolate(Quaternionf value1, Quaternionf value2, float progress) {
        // TODO: Introduce TempVars
        Vector4f vector1 = new Vector4f(value1.x(), value1.y(), value1.z(), value1.w());
        Vector4f vector2 = new Vector4f(value2.x(), value2.y(), value2.z(), value2.w());
        Vector4f vectorResult = vector1.add(vector2.sub(vector1, new Vector4f()).mul(progress), new Vector4f());
        return new Quaternionf(vectorResult.x(), vectorResult.y(), vectorResult.z(), vectorResult.w());
    }
}
