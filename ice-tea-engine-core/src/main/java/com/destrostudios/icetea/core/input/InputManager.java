package com.destrostudios.icetea.core.input;

import com.destrostudios.icetea.core.Application;
import com.destrostudios.icetea.core.lifecycle.LifecycleObject;
import lombok.Getter;
import org.joml.Vector2f;
import org.lwjgl.glfw.GLFWCharCallback;
import org.lwjgl.glfw.GLFWCursorPosCallback;
import org.lwjgl.glfw.GLFWKeyCallback;
import org.lwjgl.glfw.GLFWMouseButtonCallback;

import java.util.LinkedList;

import static org.lwjgl.glfw.GLFW.*;

public class InputManager extends LifecycleObject {

    private GLFWKeyCallback glfwKeyCallback;
    private GLFWCharCallback glfwCharacterCallback;
    private GLFWMouseButtonCallback glfwMouseButtonCallback;
    private GLFWCursorPosCallback glfwCursorPosCallback;
    private LinkedList<KeyListener> keyListeners = new LinkedList<>();
    private LinkedList<CharacterListener> characterListeners = new LinkedList<>();
    private LinkedList<KeyEvent> pendingKeyEvents = new LinkedList<>();
    private LinkedList<CharacterEvent> pendingCharacterEvents = new LinkedList<>();
    private LinkedList<MouseButtonListener> mouseButtonListeners = new LinkedList<>();
    private LinkedList<MousePositionEvent> pendingMousePositionEvents = new LinkedList<>();
    private LinkedList<MousePositionListener> mousePositionListeners = new LinkedList<>();
    private LinkedList<MouseButtonEvent> pendingMouseButtonEvents = new LinkedList<>();
    @Getter
    private Vector2f cursorPosition = new Vector2f();

    @Override
    public void init(Application application) {
        super.init(application);
        initKeyCallback();
        initCharacterCallback();
        initMouseCallback();
    }

    private void initKeyCallback() {
        glfwKeyCallback = new GLFWKeyCallback() {

            @Override
            public void invoke(long window, int key, int scanCode, int action, int modifiers) {
                pendingKeyEvents.add(new KeyEvent(key, scanCode, action, modifiers));
            }
        };
        glfwSetKeyCallback(application.getWindow(), glfwKeyCallback);
    }

    private void initCharacterCallback() {
        glfwCharacterCallback = new GLFWCharCallback() {

            @Override
            public void invoke(long window, int codepoint) {
                pendingCharacterEvents.add(new CharacterEvent(codepoint));
            }
        };
        glfwSetCharCallback(application.getWindow(), glfwCharacterCallback);
    }

    private void initMouseCallback() {
        // Buttons
        glfwMouseButtonCallback = new GLFWMouseButtonCallback() {

            @Override
            public void invoke(long window, int button, int action, int mods) {
                pendingMouseButtonEvents.add(new MouseButtonEvent(button, action, mods));
            }
        };
        glfwSetMouseButtonCallback(application.getWindow(), glfwMouseButtonCallback);
        // Position
        glfwCursorPosCallback = new GLFWCursorPosCallback() {

            @Override
            public void invoke(long window, double x, double y) {
                double deltaX = (x - cursorPosition.x());
                double deltaY = (y - cursorPosition.y());
                cursorPosition.set(x, y);
                pendingMousePositionEvents.add(new MousePositionEvent(x, y, deltaX, deltaY));
            }
        };
        glfwSetCursorPosCallback(application.getWindow(), glfwCursorPosCallback);
    }

    @Override
    public void update(Application application, int imageIndex, float tpf) {
        super.update(application, imageIndex, tpf);
        // Keys
        for (KeyEvent keyEvent : pendingKeyEvents) {
            for (KeyListener keyListener : keyListeners.toArray(new KeyListener[0])) {
                keyListener.onKeyEvent(keyEvent);
            }
        }
        pendingKeyEvents.clear();
        // Characters
        for (CharacterEvent characterEvent : pendingCharacterEvents) {
            for (CharacterListener characterListener : characterListeners.toArray(new CharacterListener[0])) {
                characterListener.onCharacterEvent(characterEvent);
            }
        }
        pendingCharacterEvents.clear();
        // Mouse buttons
        for (MouseButtonEvent mouseButtonEvent : pendingMouseButtonEvents.toArray(new MouseButtonEvent[0])) {
            for (MouseButtonListener mouseButtonListener : mouseButtonListeners.toArray(new MouseButtonListener[0])) {
                mouseButtonListener.onMouseButtonEvent(mouseButtonEvent);
            }
        }
        pendingMouseButtonEvents.clear();
        // Mouse positions
        for (MousePositionEvent mousePositionEvent : pendingMousePositionEvents.toArray(new MousePositionEvent[0])) {
            for (MousePositionListener mousePositionListener : mousePositionListeners.toArray(new MousePositionListener[0])) {
                mousePositionListener.onMousePositionEvent(mousePositionEvent);
            }
        }
        pendingMousePositionEvents.clear();
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

    public void addCharacterListener(CharacterListener characterListener) {
        characterListeners.add(characterListener);
    }

    public void removeCharacterListener(CharacterListener characterListener) {
        characterListeners.remove(characterListener);
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

    @Override
    public void cleanup() {
        glfwKeyCallback.free();
        glfwCharacterCallback.free();
        glfwMouseButtonCallback.free();
        glfwCursorPosCallback.free();
        super.cleanup();
    }
}
