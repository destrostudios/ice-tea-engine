package com.destrostudios.icetea.core.animation;

import lombok.Getter;
import lombok.Setter;

public abstract class Animation {

    @Getter
    @Setter
    private String name;

    public abstract void update(float time);
}
