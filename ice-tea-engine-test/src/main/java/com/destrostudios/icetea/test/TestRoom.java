package com.destrostudios.icetea.test;

import com.destrostudios.icetea.core.Application;
import com.destrostudios.icetea.core.asset.locator.FileLocator;
import com.destrostudios.icetea.core.camera.systems.CameraKeyMoveSystem;
import com.destrostudios.icetea.core.camera.systems.CameraMouseRotateSystem;
import com.destrostudios.icetea.core.scene.Node;
import com.destrostudios.icetea.core.scene.Spatial;
import org.joml.Vector3f;

public class TestRoom extends Application {

    public static void main(String[] args) {
        new TestRoom().start();
    }

    public TestRoom() {
        config.setEnableValidationLayer(true);
        config.setDisplayFpsInTitle(true);
    }

    @Override
    protected void init() {
        super.init();
        assetManager.addLocator(new FileLocator("./assets"));

        sceneCamera.setLocation(new Vector3f(0, 10, 5));

        Spatial room = assetManager.loadModel("models/room.gltf");

        Node nodeRoomWrapper = new Node();
        nodeRoomWrapper.add(room);
        nodeRoomWrapper.move(new Vector3f(0, 0, 0));
        nodeRoomWrapper.scale(new Vector3f(2, 2, 2));
        sceneNode.add(nodeRoomWrapper);

        addSystem(new CameraMouseRotateSystem(sceneCamera));

        CameraKeyMoveSystem cameraKeyMoveSystem = new CameraKeyMoveSystem(sceneCamera);
        cameraKeyMoveSystem.setMoveSpeed(10);
        addSystem(cameraKeyMoveSystem);
    }
}
