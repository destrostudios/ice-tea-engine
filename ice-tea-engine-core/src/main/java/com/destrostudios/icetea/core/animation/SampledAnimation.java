package com.destrostudios.icetea.core.animation;

public abstract class SampledAnimation<T> extends Animation {

    public SampledAnimation(AnimationSampler<T> sampler) {
        this.sampler = sampler;
    }
    private AnimationSampler<T> sampler;

    @Override
    public void update(float tpf) {
        super.update(tpf);
        if (sampler.isFinished(time)) {
            // TODO: Finish, handle loop mode
        } else {
            T value = sampler.getValue(time);
            setValue(value);
        }
    }

    protected abstract void setValue(T value);
}
