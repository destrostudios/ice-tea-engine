package com.destrostudios.icetea.core.camera.systems;

import com.destrostudios.icetea.core.Application;
import com.destrostudios.icetea.core.camera.SceneCamera;
import com.destrostudios.icetea.core.input.KeyEvent;
import com.destrostudios.icetea.core.input.KeyListener;
import com.destrostudios.icetea.core.system.AppSystem;
import lombok.Getter;
import lombok.Setter;
import org.joml.Vector2f;
import org.joml.Vector3f;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.glfw.GLFW.GLFW_RELEASE;

public class CameraKeyMoveSystem extends AppSystem implements KeyListener {

    public CameraKeyMoveSystem(SceneCamera sceneCamera) {
        this.sceneCamera = sceneCamera;
        moveDirection = new Vector2f();
    }
    @Getter
    private SceneCamera sceneCamera;
    @Getter
    @Setter
    private float moveSpeed = 3;
    private Vector2f moveDirection;

    @Override
    public void initialize(Application application) {
        super.initialize(application);
        application.getInputManager().addKeyListener(this);
    }

    @Override
    public void cleanup() {
        super.cleanup();
        application.getInputManager().removeKeyListener(this);
    }

    @Override
    public void onKeyEvent(KeyEvent keyEvent) {
        // Set camera move direction
        Integer axis = null;
        Integer value = null;
        switch (keyEvent.getKey()) {
            case GLFW_KEY_W:
                axis = 1;
                value = 1;
                break;
            case GLFW_KEY_D:
                axis = 0;
                value = 1;
                break;
            case GLFW_KEY_S:
                axis = 1;
                value = -1;
                break;
            case GLFW_KEY_A:
                axis = 0;
                value = -1;
                break;
        }
        if (axis != null) {
            Integer factor = null;
            if (keyEvent.getAction() == GLFW_PRESS) {
                factor = 1;
            } else if (keyEvent.getAction() == GLFW_RELEASE) {
                factor = 0;
            }
            if (factor != null) {
                moveDirection.setComponent(axis, factor * value);
            }
        }
    }

    @Override
    public void update(float tpf) {
        super.update(tpf);
        // Avoid unnecessary vector and matrix recalculations
        if (moveDirection.lengthSquared() > 0) {
            Vector3f deltaRight = sceneCamera.getRight().mul(tpf * moveSpeed * moveDirection.x());
            Vector3f deltaForward = sceneCamera.getBack().mul(-1 * tpf * moveSpeed * moveDirection.y());
            sceneCamera.setLocation(sceneCamera.getLocation().add(deltaRight).add(deltaForward));
        }
    }
}
