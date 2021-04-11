package com.destrostudios.icetea.core.animation;

public class CombinedAnimation extends Animation {

    public CombinedAnimation(Animation[] animations) {
        this.animations = animations;
    }
    private Animation[] animations;

    @Override
    public void update(float time) {
        for (Animation animation : animations) {
            animation.update(time);
        }
    }
}
