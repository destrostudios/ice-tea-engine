package com.destrostudios.icetea.core.animation;

import com.destrostudios.icetea.core.clone.CloneContext;

public abstract class SampledAnimation<T> extends Animation {

    public SampledAnimation(AnimationSampler<T> sampler) {
        this.sampler = sampler;
    }

    public SampledAnimation(SampledAnimation<T> sampledAnimation) {
        super(sampledAnimation);
        this.sampler = sampledAnimation.sampler;
    }
    private AnimationSampler<T> sampler;

    @Override
    public void update(float time) {
        if (sampler.isFinished(time)) {
            // TODO: Finish, handle loop mode
        } else {
            T value = sampler.getValue(time);
            // TODO: Something with null handling has to be done here, to be analyzed how the desired architecture looks like
            setValue(value);
        }
    }

    protected abstract void setValue(T value);

    @Override
    public abstract SampledAnimation<T> clone(CloneContext context);
}
