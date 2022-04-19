package com.destrostudios.icetea.core.camera.systems;

import com.destrostudios.icetea.core.camera.SceneCamera;
import com.destrostudios.icetea.core.input.MousePositionEvent;
import com.destrostudios.icetea.core.input.MousePositionListener;
import com.destrostudios.icetea.core.lifecycle.LifecycleObject;
import lombok.Getter;
import lombok.Setter;
import org.joml.Matrix3f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import static org.lwjgl.glfw.GLFW.GLFW_CURSOR_DISABLED;
import static org.lwjgl.glfw.GLFW.GLFW_CURSOR_NORMAL;

public class CameraMouseRotateSystem extends LifecycleObject implements MousePositionListener {

    public CameraMouseRotateSystem(SceneCamera sceneCamera) {
        this.sceneCamera = sceneCamera;
    }
    @Getter
    private SceneCamera sceneCamera;
    private Vector3f initialUp;
    @Getter
    @Setter
    private float rotationSpeed = ((-1 / 1024f) * (float) Math.PI);

    @Override
    protected void init() {
        super.init();
        application.getInputManager().addMousePositionListener(this);
        application.getInputManager().setCursorMode(GLFW_CURSOR_DISABLED);
    }

    @Override
    public void onMousePositionEvent(MousePositionEvent mousePositionEvent) {
        // Ignore the very first event, since the delta will be too huge - Instead use the timing to get the initial up vector
        if (initialUp == null) {
            initialUp = sceneCamera.getUp();
        } else {
            float angleX = (float) (mousePositionEvent.getDeltaX() * rotationSpeed);
            float angleY = (float) (mousePositionEvent.getDeltaY() * rotationSpeed);
            rotateCamera(angleX, initialUp);
            rotateCamera(angleY, sceneCamera.getRight());
        }
    }

    private void rotateCamera(float angle, Vector3f axis){
        Matrix3f matrix = new Matrix3f().rotation(angle, axis);

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

    @Override
    protected void cleanupInternal() {
        application.getInputManager().removeMousePositionListener(this);
        application.getInputManager().setCursorMode(GLFW_CURSOR_NORMAL);
        super.cleanupInternal();
    }
}
