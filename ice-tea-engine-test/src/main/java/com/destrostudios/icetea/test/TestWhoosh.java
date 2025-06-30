package com.destrostudios.icetea.test;

import com.destrostudios.icetea.core.Application;
import com.destrostudios.icetea.core.animation.AnimationControl;
import com.destrostudios.icetea.core.asset.loader.GltfLoaderSettings;
import com.destrostudios.icetea.core.asset.locator.FileLocator;
import com.destrostudios.icetea.core.collision.CollisionResult;
import com.destrostudios.icetea.core.collision.Ray;
import com.destrostudios.icetea.core.light.DirectionalLight;
import com.destrostudios.icetea.core.material.Material;
import com.destrostudios.icetea.core.mesh.Quad;
import com.destrostudios.icetea.core.render.shadow.ShadowConfig;
import com.destrostudios.icetea.core.render.shadow.ShadowMode;
import com.destrostudios.icetea.core.scene.Geometry;
import com.destrostudios.icetea.samples.whoosh.WhooshConfig;
import com.destrostudios.icetea.samples.whoosh.WhooshControl;
import org.joml.AxisAngle4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.joml.Vector4f;

import java.util.ArrayList;

import static org.lwjgl.glfw.GLFW.*;

public class TestWhoosh extends Application {

    public static void main(String[] args) {
        new TestWhoosh().start();
    }

    public TestWhoosh() {
        config.setEnableValidationLayer(true);
        config.setDisplayFpsInTitle(true);
    }
    private Geometry ground;
    private Geometry model;

    @Override
    protected void init() {
        super.init();
        assetManager.addLocator(new FileLocator("./assets"));

        sceneCamera.setLocation(new Vector3f(0, 3, 15));
        sceneCamera.setRotation(new Quaternionf().rotationX((float) 0.3f));

        DirectionalLight directionalLight = new DirectionalLight();
        directionalLight.setDirection(new Vector3f(-1, -1, -1).normalize());
        directionalLight.enableShadows(new ShadowConfig());
        setLight(directionalLight);
        sceneNode.setAffectedByLight(true);

        Material materialGround = new Material();
        materialGround.setDefaultShaders();
        materialGround.getParameters().setVector4f("color", new Vector4f(1, 1, 1, 1));

        ground = new Geometry();
        ground.setMesh(new Quad(20, 20));
        ground.setMaterial(materialGround);
        ground.move(new Vector3f(-10, -0.25f, 10));
        ground.rotate(new Quaternionf(new AxisAngle4f((float) (Math.PI / -2), 1, 0, 0)));
        ground.setShadowMode(ShadowMode.RECEIVE);
        sceneNode.add(ground);

        model = (Geometry) assetManager.loadModel("models/ghost/ghost.gltf", GltfLoaderSettings.builder().bakeGeometries(true).build());
        model.scale(new Vector3f(100, 100, 100));
        model.setShadowMode(ShadowMode.CAST_AND_RECEIVE);
        AnimationControl animationControl = model.getFirstControl(AnimationControl.class);
        animationControl.play("cast_spell");
        sceneNode.add(model);

        WhooshControl whooshControl = new WhooshControl(new WhooshConfig());
        model.addControl(whooshControl);

        inputManager.addMouseButtonListener(mouseButtonEvent -> {
            if (mouseButtonEvent.getAction() == GLFW_PRESS) {
                Vector3f groundLocation = getCursorGroundLocation();
                if (groundLocation != null) {
                    whooshControl.whoosh(groundLocation);
                }
            }
        });
    }

    private Vector3f getCursorGroundLocation() {
        Vector3f worldCoordinatesFront = getWorldCoordinates(inputManager.getCursorPosition(), 0);
        Vector3f worldCoordinatesBack = getWorldCoordinates(inputManager.getCursorPosition(), 1);
        Vector3f rayDirection = worldCoordinatesBack.sub(worldCoordinatesFront, new Vector3f());
        Ray ray = new Ray(worldCoordinatesFront, rayDirection);

        ArrayList<CollisionResult> collisionResults = new ArrayList<>();
        sceneNode.collideStatic(ray, collisionResults);

        for (CollisionResult collisionResult : collisionResults) {
            if (collisionResult.getGeometry() == ground) {
                return collisionResult.getPosition();
            }
        }
        return null;
    }
}
