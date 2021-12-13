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
    private List<MousePositionListener> mousePositionListeners;
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
        mousePositionListeners = new LinkedList<>();
        glfwCursorPosCallback = new GLFWCursorPosCallback() {

            @Override
            public void invoke(long window, double x, double y) {
                double deltaX = (x - cursorPosition.x());
                double deltaY = (y - cursorPosition.y());
                cursorPosition.set(x, y);
                mousePositionListeners.forEach(mousePositionListener -> mousePositionListener.onMousePositionEvent(new MousePositionEvent(x, y, deltaX, deltaY)));
            }
        };
        glfwSetCursorPosCallback(application.getWindow(), glfwCursorPosCallback);
    }

    public void setCursorMode(int cursorMode) {
        glfwSetInputMode(application.getWindow(), GLFW_CURSOR, cursorMode);
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

    public void addMousePositionListener(MousePositionListener mousePositionListener) {
        mousePositionListeners.add(mousePositionListener);
    }

    public void removeMousePositionListener(MousePositionListener mousePositionListener) {
        mousePositionListeners.remove(mousePositionListener);
    }

    public void cleanup() {
        glfwKeyCallback.free();
        glfwMouseButtonCallback.free();
        glfwCursorPosCallback.free();
    }
}
