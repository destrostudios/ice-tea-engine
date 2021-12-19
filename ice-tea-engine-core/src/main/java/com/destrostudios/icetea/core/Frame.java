package com.destrostudios.icetea.core;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class Frame {
    private long imageAvailableSemaphore;
    private long renderFinishedSemaphore;
    private long fence;
}
