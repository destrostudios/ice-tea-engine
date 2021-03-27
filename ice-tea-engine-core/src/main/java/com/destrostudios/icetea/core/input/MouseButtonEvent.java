package com.destrostudios.icetea.core.input;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * See org.lwjgl.glfw.GLFWMouseButtonCallbackI.invoke
 */
@AllArgsConstructor
@Getter
public class MouseButtonEvent {
    private int button;
    private int action;
    private int modifiers;
}
