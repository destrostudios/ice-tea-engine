package com.destrostudios.icetea.core.input;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class MousePositionEvent extends Event {
    private double x;
    private double y;
    private double deltaX;
    private double deltaY;
}
