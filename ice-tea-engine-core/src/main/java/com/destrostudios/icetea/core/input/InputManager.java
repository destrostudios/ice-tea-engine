package com.destrostudios.icetea.core.input;

import com.destrostudios.icetea.core.lifecycle.LifecycleObject;
import lombok.Getter;
import org.joml.Vector2f;
import org.lwjgl.glfw.*;

import java.util.LinkedList;
import java.util.List;
import java.util.function.BiConsumer;

import static org.lwjgl.glfw.GLFW.*;

public class InputManager extends LifecycleObject {

    private LinkedList<KeyListener> keyListeners = new LinkedList<>();
    private LinkedList<KeyEvent> pendingKeyEvents = new LinkedList<>();
    private LinkedList<CharacterListener> characterListeners = new LinkedList<>();
    private LinkedList<CharacterEvent> pendingCharacterEvents = new LinkedList<>();
    private LinkedList<MousePositionListener> mousePositionListeners = new LinkedList<>();
    private LinkedList<MousePositionEvent> pendingMousePositionEvents = new LinkedList<>();
    private LinkedList<MouseButtonListener> mouseButtonListeners = new LinkedList<>();
    private LinkedList<MouseButtonEvent> pendingMouseButtonEvents = new LinkedList<>();
    private LinkedList<MouseScrollListener> mouseScrollListeners = new LinkedList<>();
    private LinkedList<MouseScrollEvent> pendingMouseScrollEvents = new LinkedList<>();
    @Getter
    private Vector2f cursorPosition = new Vector2f();

    @Override
    protected void init() {
        super.init();
        glfwSetKeyCallback(application.getWindow(), new GLFWKeyCallback() {

            @Override
            public void invoke(long window, int key, int scanCode, int action, int modifiers) {
                pendingKeyEvents.add(new KeyEvent(key, scanCode, action, modifiers));
            }
        });
        glfwSetCharCallback(application.getWindow(), new GLFWCharCallback() {

            @Override
            public void invoke(long window, int codepoint) {
                pendingCharacterEvents.add(new CharacterEvent(codepoint));
            }
        });
        glfwSetCursorPosCallback(application.getWindow(), new GLFWCursorPosCallback() {

            @Override
            public void invoke(long window, double x, double y) {
                double deltaX = (x - cursorPosition.x());
                double deltaY = (y - cursorPosition.y());
                cursorPosition.set(x, y);
                pendingMousePositionEvents.add(new MousePositionEvent(x, y, deltaX, deltaY));
            }
        });
        glfwSetMouseButtonCallback(application.getWindow(), new GLFWMouseButtonCallback() {

            @Override
            public void invoke(long window, int button, int action, int mods) {
                pendingMouseButtonEvents.add(new MouseButtonEvent(button, action, mods));
            }
        });
        glfwSetScrollCallback(application.getWindow(), new GLFWScrollCallback() {

            @Override
            public void invoke(long window, double xOffset, double yOffset) {
                pendingMouseScrollEvents.add(new MouseScrollEvent(xOffset, yOffset));
            }
        });
    }

    @Override
    public void update(float tpf) {
        super.update(tpf);
        processEvents(pendingKeyEvents, keyListeners, KeyListener::onKeyEvent);
        processEvents(pendingCharacterEvents, characterListeners, CharacterListener::onCharacterEvent);
        processEvents(pendingMouseButtonEvents, mouseButtonListeners, MouseButtonListener::onMouseButtonEvent);
        processEvents(pendingMousePositionEvents, mousePositionListeners, MousePositionListener::onMousePositionEvent);
        processEvents(pendingMouseScrollEvents, mouseScrollListeners, MouseScrollListener::onMouseScrollEvent);
    }

    private <E extends Event, L> void processEvents(List<E> events, List<L> listeners, BiConsumer<L, E> onEvent) {
        for (E event : events) {
            // Copy listeners before iteration because they might add/remove listeners
            for (Object listener : listeners.toArray()) {
                if (event.isStopPropagating()) {
                    break;
                }
                onEvent.accept((L) listener, event);
            }
        }
        events.clear();
    }

    public void setCursorMode(int cursorMode) {
        glfwSetInputMode(application.getWindow(), GLFW_CURSOR, cursorMode);
    }

    public void addKeyListener(KeyListener keyListener) {
        addKeyListener(keyListener, keyListeners.size());
    }

    public void addKeyListener(KeyListener keyListener, int index) {
        keyListeners.add(index, keyListener);
    }

    public void removeKeyListener(KeyListener keyListener) {
        keyListeners.remove(keyListener);
    }

    public void addCharacterListener(CharacterListener characterListener) {
        addCharacterListener(characterListener, characterListeners.size());
    }

    public void addCharacterListener(CharacterListener characterListener, int index) {
        characterListeners.add(index, characterListener);
    }

    public void removeCharacterListener(CharacterListener characterListener) {
        characterListeners.remove(characterListener);
    }

    public void addMousePositionListener(MousePositionListener mousePositionListener) {
        addMousePositionListener(mousePositionListener, mousePositionListeners.size());
    }

    public void addMousePositionListener(MousePositionListener mousePositionListener, int index) {
        mousePositionListeners.add(index, mousePositionListener);
    }

    public void removeMousePositionListener(MousePositionListener mousePositionListener) {
        mousePositionListeners.remove(mousePositionListener);
    }

    public void addMouseButtonListener(MouseButtonListener mouseButtonListener) {
        addMouseButtonListener(mouseButtonListener, mouseButtonListeners.size());
    }

    public void addMouseButtonListener(MouseButtonListener mouseButtonListener, int index) {
        mouseButtonListeners.add(index, mouseButtonListener);
    }

    public void removeMouseButtonListener(MouseButtonListener mouseButtonListener) {
        mouseButtonListeners.remove(mouseButtonListener);
    }

    public void addMouseScrollListener(MouseScrollListener mouseScrollListener) {
        addMouseScrollListener(mouseScrollListener, mouseButtonListeners.size());
    }

    public void addMouseScrollListener(MouseScrollListener mouseScrollListener, int index) {
        mouseScrollListeners.add(index, mouseScrollListener);
    }

    public void removeMouseScrollListener(MouseScrollListener mouseScrollListener) {
        mouseScrollListeners.remove(mouseScrollListener);
    }

    @Override
    protected void cleanupInternal() {
        glfwSetKeyCallback(application.getWindow(), null);
        glfwSetCharCallback(application.getWindow(), null);
        glfwSetCursorPosCallback(application.getWindow(), null);
        glfwSetMouseButtonCallback(application.getWindow(), null);
        glfwSetScrollCallback(application.getWindow(), null);
        super.cleanupInternal();
    }
}
