package com.destrostudios.icetea.core.animation;

import com.destrostudios.icetea.core.Application;
import com.destrostudios.icetea.core.clone.CloneContext;
import com.destrostudios.icetea.core.scene.Control;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.stream.Collectors;

public class AnimationControl extends Control {

    public AnimationControl(List<? extends Animation> animations) {
        this.animations = animations;
    }

    public AnimationControl(AnimationControl animationControl, CloneContext context) {
        animations = animationControl.animations.stream().map(context::cloneByReference).collect(Collectors.toList());
        playingAnimation = context.cloneByReference(animationControl.playingAnimation);
        time = animationControl.time;
        speed = animationControl.speed;
        playing = animationControl.playing;
        needsUpdate = animationControl.needsUpdate;
    }
    @Getter
    private List<? extends Animation> animations;
    @Getter
    private Animation playingAnimation;
    @Getter
    private float time;
    @Getter
    @Setter
    private float speed = 1;
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
    public void update(Application application, int imageIndex, float tpf) {
        super.update(application, imageIndex, tpf);
        if (playing) {
            time += tpf * speed;
            needsUpdate = true;
        }
        if (needsUpdate) {
            playingAnimation.update(time);
            needsUpdate = false;
        }
    }

    @Override
    public AnimationControl clone(CloneContext context) {
        return new AnimationControl(this, context);
    }
}
