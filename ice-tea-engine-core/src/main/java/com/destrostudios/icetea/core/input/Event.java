package com.destrostudios.icetea.core.input;

import lombok.Getter;

@Getter
public class Event {

    private boolean stopPropagating;

    public void stopPropagating() {
        stopPropagating = true;
    }
}
