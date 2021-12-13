package com.destrostudios.icetea.core.camera;

import com.destrostudios.icetea.core.input.InputManager;
import com.destrostudios.icetea.core.input.MousePositionEvent;
import com.destrostudios.icetea.core.input.MousePositionListener;
import lombok.Setter;
import org.joml.Matrix3f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import static org.lwjgl.glfw.GLFW.GLFW_CURSOR_DISABLED;
import static org.lwjgl.glfw.GLFW.GLFW_CURSOR_NORMAL;

public class FreeCamera implements MousePositionListener {

    public FreeCamera(SceneCamera sceneCamera) {
        this.sceneCamera = sceneCamera;
        initialUp = sceneCamera.getUp();
    }
    private SceneCamera sceneCamera;
    private Vector3f initialUp;
    @Setter
    private float rotationSpeed = ((-1 / 1024f) * (float) Math.PI);

    public void add(InputManager inputManager) {
        inputManager.addMousePositionListener(this);
        inputManager.setCursorMode(GLFW_CURSOR_DISABLED);
    }

    public void remove(InputManager inputManager) {
        inputManager.removeMousePositionListener(this);
        inputManager.setCursorMode(GLFW_CURSOR_NORMAL);
    }

    @Override
    public void onMousePositionEvent(MousePositionEvent mousePositionEvent) {
        float valueX = (float) (mousePositionEvent.getDeltaX() * rotationSpeed);
        float valueY = (float) (mousePositionEvent.getDeltaY() * rotationSpeed);
        rotateCamera(valueX, initialUp);
        rotateCamera(valueY, sceneCamera.getRight());
    }

    private void rotateCamera(float value, Vector3f axis){
        Matrix3f matrix = new Matrix3f().rotation(value, axis);

        Vector3f up = sceneCamera.getUp();
        Vector3f right = sceneCamera.getRight();
        Vector3f back = sceneCamera.getBack();

        up.mul(matrix);
        right.mul(matrix);
        back.mul(matrix);

        Quaternionf rotation = new Quaternionf();
        matrix.set(
            right.x(), right.y(), right.z(),
            up.x(), up.y(), up.z(),
            back.x(), back.y(), back.z()
        );
        rotation.setFromUnnormalized(matrix);
        rotation.normalize();
        // TODO: For some reason, we have to invert w - I assume because this is right-handed vs left-handed, should be confirmed
        rotation.set(rotation.x(), rotation.y(), rotation.z(), -1 * rotation.w());

        sceneCamera.setRotation(rotation);
    }
}
