package com.destrostudios.icetea.core.animation;

import com.destrostudios.icetea.core.scene.Control;
import lombok.Getter;

public class AnimationControl extends Control {

    public AnimationControl(Animation[] animations) {
        this.animations = animations;
    }
    @Getter
    private Animation[] animations;
    @Getter
    private PlayingAnimation playingAnimation;

    public void play(int index) {
        playingAnimation = new PlayingAnimation(animations[index]);
    }

    @Override
    public void update(float tpf) {
        super.update(tpf);
        if (playingAnimation != null) {
            playingAnimation.update(tpf);
        }
    }
}
