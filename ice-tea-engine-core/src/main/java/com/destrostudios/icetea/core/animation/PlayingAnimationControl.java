package com.destrostudios.icetea.core.animation;

import com.destrostudios.icetea.core.scene.Control;

public class PlayingAnimationControl extends Control {

    public PlayingAnimationControl(Animation animation) {
        this.animation = animation;
    }
    private Animation animation;

    @Override
    public void update(float tpf) {
        super.update(tpf);
        animation.update(tpf);
    }
}
