package com.destrostudios.icetea.core.animation;

public class PlayingAnimation {

    public PlayingAnimation(Animation animation) {
        this.animation = animation;
    }
    private Animation animation;
    private float time;

    public void update(float tpf) {
        time += tpf;
        animation.update(time);
    }
}
