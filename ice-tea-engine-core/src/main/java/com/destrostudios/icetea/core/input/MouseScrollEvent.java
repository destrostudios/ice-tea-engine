package com.destrostudios.icetea.core.input;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * See org.lwjgl.glfw.GLFWScrollCallbackI.invoke
 */
@AllArgsConstructor
@Getter
public class MouseScrollEvent extends Event {
    private double xOffset;
    private double yOffset;
}
