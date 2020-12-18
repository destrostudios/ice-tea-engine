package com.destrostudios.icetea.test;

import com.destrostudios.icetea.core.Application;
import com.destrostudios.icetea.core.GltfModelLoader;
import com.destrostudios.icetea.core.Node;
import com.destrostudios.icetea.core.filters.RadialBlurFilter;
import com.destrostudios.icetea.core.filters.SepiaFilter;
import com.destrostudios.icetea.core.lights.DirectionalLight;
import org.joml.Vector3f;

import static org.lwjgl.glfw.GLFW.*;

public class TestRoom extends Application {

    public static void main(String[] args) {
        new TestRoom().start();
    }

    private Vector3f cameraMoveDirection = new Vector3f();

    @Override
    protected void initScene() {
        camera.setLocation(new Vector3f(0, 0, 7));
        camera.setRotation(new Vector3f(-88, 0, 0));

        DirectionalLight directionalLight = new DirectionalLight();
        directionalLight.setDirection(new Vector3f(1, 0, -0.25f).normalize());
        directionalLight.addAffectedSpatial(rootNode);
        // directionalLight.addShadows(2048);
        // setLight(directionalLight);

        // Room

        Node nodeRoom = new GltfModelLoader("models/room.gltf").load();

        Node nodeRoomWrapper = new Node();
        nodeRoomWrapper.add(nodeRoom);
        nodeRoomWrapper.move(new Vector3f(0, 0, 0));
        nodeRoomWrapper.scale(new Vector3f(2, 2, 2));
        rootNode.add(nodeRoomWrapper);

        RadialBlurFilter radialBlurFilter = new RadialBlurFilter();
        SepiaFilter sepiaFilter = new SepiaFilter();

        addKeyListener(keyEvent -> {
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
        camera.setLocation(camera.getLocation().add(cameraMoveDirection.mul(tpf * 10, new Vector3f())));
        camera.setRotation(camera.getRotation().add(new Vector3f(0, 0, tpf * 40)));
    }
}
