package com.destrostudios.icetea.core.input;

import com.destrostudios.icetea.core.Application;
import lombok.Getter;
import org.joml.Vector2f;
import org.lwjgl.glfw.GLFWCursorPosCallback;
import org.lwjgl.glfw.GLFWKeyCallback;
import org.lwjgl.glfw.GLFWMouseButtonCallback;

import java.util.LinkedList;
import java.util.List;

import static org.lwjgl.glfw.GLFW.*;

public class InputManager {

    public InputManager(Application application) {
        this.application = application;
    }
    private Application application;
    private GLFWKeyCallback glfwKeyCallback;
    private GLFWMouseButtonCallback glfwMouseButtonCallback;
    private GLFWCursorPosCallback glfwCursorPosCallback;
    private List<KeyListener> keyListeners;
    private List<MouseButtonListener> mouseButtonListeners;
    @Getter
    private Vector2f cursorPosition;

    public void init() {
        initKeyListeners();
        initMouseListeners();
    }

    private void initKeyListeners() {
        keyListeners = new LinkedList<>();
        glfwKeyCallback = new GLFWKeyCallback() {

            @Override
            public void invoke(long window, int key, int scanCode, int action, int modifiers) {
                keyListeners.forEach(keyListener -> keyListener.onKeyEvent(new KeyEvent(key, scanCode, action, modifiers)));
            }
        };
        glfwSetKeyCallback(application.getWindow(), glfwKeyCallback);
    }

    private void initMouseListeners() {
        // Buttons
        mouseButtonListeners = new LinkedList<>();
        glfwMouseButtonCallback = new GLFWMouseButtonCallback() {

            @Override
            public void invoke(long window, int button, int action, int mods) {
                mouseButtonListeners.forEach(mouseButtonListener -> mouseButtonListener.onMouseButtonEvent(new MouseButtonEvent(button, action, mods)));
            }
        };
        glfwSetMouseButtonCallback(application.getWindow(), glfwMouseButtonCallback);
        // Position
        cursorPosition = new Vector2f();
        glfwCursorPosCallback = new GLFWCursorPosCallback() {

            @Override
            public void invoke(long window, double xpos, double ypos) {
                cursorPosition.set(xpos, ypos);
            }
        };
        glfwSetCursorPosCallback(application.getWindow(), glfwCursorPosCallback);
    }

    public void addKeyListener(KeyListener keyListener) {
        keyListeners.add(keyListener);
    }

    public void removeKeyListener(KeyListener keyListener) {
        keyListeners.remove(keyListener);
    }

    public void addMouseButtonListener(MouseButtonListener mouseButtonListener) {
        mouseButtonListeners.add(mouseButtonListener);
    }

    public void removeMouseButtonListener(MouseButtonListener mouseButtonListener) {
        mouseButtonListeners.remove(mouseButtonListener);
    }

    public void cleanup() {
        glfwKeyCallback.free();
        glfwMouseButtonCallback.free();
        glfwCursorPosCallback.free();
    }
}
