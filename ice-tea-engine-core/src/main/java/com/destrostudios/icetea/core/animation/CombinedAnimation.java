package com.destrostudios.icetea.core.animation;

import com.destrostudios.icetea.core.clone.CloneContext;

public class CombinedAnimation extends Animation {

    public CombinedAnimation(Animation[] animations) {
        this.animations = animations;
    }

    public CombinedAnimation(CombinedAnimation combinedAnimation, CloneContext context) {
        super(combinedAnimation);
        animations = new Animation[combinedAnimation.animations.length];
        for (int i = 0; i < animations.length; i++) {
            animations[i] = combinedAnimation.animations[i].clone(context);
        }
    }
    private Animation[] animations;

    @Override
    public void update(float time) {
        for (Animation animation : animations) {
            animation.update(time);
        }
    }

    @Override
    public float getDuration() {
        float maximumDuration = 0;
        for (Animation animation : animations) {
            float duration = animation.getDuration();
            if (duration > maximumDuration) {
                maximumDuration = duration;
            }
        }
        return maximumDuration;
    }

    @Override
    public CombinedAnimation clone(CloneContext context) {
        return new CombinedAnimation(this, context);
    }
}
