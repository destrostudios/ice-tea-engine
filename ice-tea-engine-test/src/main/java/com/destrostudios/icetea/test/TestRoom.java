package com.destrostudios.icetea.test;

import com.destrostudios.icetea.core.Application;
import com.destrostudios.icetea.core.scene.Node;
import com.destrostudios.icetea.core.filter.RadialBlurFilter;
import com.destrostudios.icetea.core.filter.SepiaFilter;
import com.destrostudios.icetea.core.scene.Spatial;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import static org.lwjgl.glfw.GLFW.*;

public class TestRoom extends Application {

    public static void main(String[] args) {
        new TestRoom().start();
    }

    private Vector3f cameraMoveDirection = new Vector3f();

    @Override
    protected void initScene() {
        sceneCamera.setLocation(new Vector3f(0, 0, 7));
        sceneCamera.setRotation(new Quaternionf().rotateLocalX((float) (-0.49f * Math.PI)));

        // Room

        Spatial room = assetManager.loadModel("models/room.gltf");

        Node nodeRoomWrapper = new Node();
        nodeRoomWrapper.add(room);
        nodeRoomWrapper.move(new Vector3f(0, 0, 0));
        nodeRoomWrapper.scale(new Vector3f(2, 2, 2));
        sceneNode.add(nodeRoomWrapper);

        RadialBlurFilter radialBlurFilter = new RadialBlurFilter();
        SepiaFilter sepiaFilter = new SepiaFilter();

        inputManager.addKeyListener(keyEvent -> {
            // Add/Remove filters
            switch (keyEvent.getKey()) {
                case GLFW_KEY_1:
                    if (keyEvent.getAction() == GLFW_PRESS) {
                        addFilter(radialBlurFilter);
                    } else if (keyEvent.getAction() == GLFW_RELEASE) {
                        removeFilter(radialBlurFilter);
                    }
                    break;
                case GLFW_KEY_2:
                    if (keyEvent.getAction() == GLFW_PRESS) {
                        addFilter(sepiaFilter);
                    } else if (keyEvent.getAction() == GLFW_RELEASE) {
                        removeFilter(sepiaFilter);
                    }
                    break;
            }
            // Set camera move direction
            Integer axis = null;
            Integer value = null;
            if (keyEvent.getKey() == GLFW_KEY_W) {
                axis = 1;
                value = 1;
            } else if (keyEvent.getKey() == GLFW_KEY_D) {
                axis = 0;
                value = 1;
            } else if (keyEvent.getKey() == GLFW_KEY_S) {
                axis = 1;
                value = -1;
            } else if (keyEvent.getKey() == GLFW_KEY_A) {
                axis = 0;
                value = -1;
            }
            if (axis != null) {
                Integer factor = null;
                if (keyEvent.getAction() == GLFW_PRESS) {
                    factor = 1;
                } else if (keyEvent.getAction() == GLFW_RELEASE) {
                    factor = 0;
                }
                if (factor != null) {
                    cameraMoveDirection.setComponent(axis, factor * value);
                }
            }
        });
    }

    @Override
    protected void update(float tpf) {
        sceneCamera.setLocation(sceneCamera.getLocation().add(cameraMoveDirection.mul(tpf * 10, new Vector3f())));
        sceneCamera.setRotation(sceneCamera.getRotation().rotateLocalY(tpf * (float) (0.1666f * Math.PI)));
    }
}
