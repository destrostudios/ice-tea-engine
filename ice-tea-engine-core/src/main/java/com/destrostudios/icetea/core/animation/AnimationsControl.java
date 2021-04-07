package com.destrostudios.icetea.core.animation;

import com.destrostudios.icetea.core.scene.Control;
import lombok.Getter;

public class AnimationsControl extends Control {

    public AnimationsControl(Animation[] animations) {
        this.animations = animations;
    }
    @Getter
    private Animation[] animations;
}
