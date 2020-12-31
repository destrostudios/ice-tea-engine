package com.destrostudios.icetea.core.input;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * See org.lwjgl.glfw.GLFWKeyCallbackI.invoke
 */
@AllArgsConstructor
@Getter
public class KeyEvent {
    private int key;
    private int scanCode;
    private int action;
    private int modifiers;
}
