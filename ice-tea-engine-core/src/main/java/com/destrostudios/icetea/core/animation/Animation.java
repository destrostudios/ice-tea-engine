package com.destrostudios.icetea.core.animation;

import com.destrostudios.icetea.core.clone.CloneContext;
import com.destrostudios.icetea.core.clone.ContextCloneable;
import lombok.Getter;
import lombok.Setter;

public abstract class Animation implements ContextCloneable {

    public Animation() { }

    public Animation(Animation animation) {
        name = animation.name;
    }
    @Getter
    @Setter
    private String name;

    public abstract void update(float time);

    @Override
    public abstract Animation clone(CloneContext context);
}
