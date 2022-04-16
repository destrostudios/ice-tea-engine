package com.destrostudios.icetea.core.input;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * See org.lwjgl.glfw.GLFWCharCallbackI.invoke
 */
@AllArgsConstructor
@Getter
public class CharacterEvent {
    private int codepoint;
}
