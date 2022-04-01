package com.destrostudios.icetea.core.animation;

import com.destrostudios.icetea.core.scene.Control;
import lombok.Getter;

import java.util.List;

public class AnimationControl extends Control {

    public AnimationControl(List<? extends Animation> animations) {
        this.animations = animations;
    }
    @Getter
    private List<? extends Animation> animations;
    @Getter
    private Animation playingAnimation;
    @Getter
    private float time;
    @Getter
    private boolean playing;
    private boolean needsUpdate;

    public void play(int index) {
        play(animations.get(index));
    }

    public void play(String animationName) {
        play(getAnimation(animationName));
    }

    public Animation getAnimation(String animationName) {
        return animations.stream().filter(animation -> animationName.equals(animation.getName())).findFirst().orElse(null);
    }

    private void play(Animation animation) {
        playingAnimation = animation;
        time = 0;
        playing = true;
    }

    public void setPlaying(boolean playing) {
        this.playing = playing;
        needsUpdate = true;
    }

    public void setTime(float time) {
        this.time = time;
        needsUpdate = true;
    }

    @Override
    public void update(float tpf) {
        super.update(tpf);
        if (playing) {
            time += tpf;
            needsUpdate = true;
        }
        if (needsUpdate) {
            playingAnimation.update(time);
            needsUpdate = false;
        }
    }
}
