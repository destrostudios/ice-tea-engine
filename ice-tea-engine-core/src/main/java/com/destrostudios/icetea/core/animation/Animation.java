package com.destrostudios.icetea.core.animation;

public abstract class Animation {

    protected float time;

    public void update(float tpf) {
        time += tpf;
    }
}
