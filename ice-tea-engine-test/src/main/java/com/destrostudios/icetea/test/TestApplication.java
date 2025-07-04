package com.destrostudios.icetea.test;

import com.destrostudios.icetea.core.*;
import com.destrostudios.icetea.core.animation.AnimationControl;
import com.destrostudios.icetea.core.asset.loader.GltfLoaderSettings;
import com.destrostudios.icetea.core.asset.locator.FileLocator;
import com.destrostudios.icetea.core.camera.systems.CameraKeyMoveSystem;
import com.destrostudios.icetea.core.camera.systems.CameraMouseRotateSystem;
import com.destrostudios.icetea.core.clone.CloneContext;
import com.destrostudios.icetea.core.collision.BoundingBox;
import com.destrostudios.icetea.core.collision.CollisionResult;
import com.destrostudios.icetea.core.collision.Ray;
import com.destrostudios.icetea.core.font.BitmapFont;
import com.destrostudios.icetea.core.font.BitmapText;
import com.destrostudios.icetea.core.light.*;
import com.destrostudios.icetea.core.material.Material;
import com.destrostudios.icetea.core.mesh.*;
import com.destrostudios.icetea.core.render.bucket.RenderBucketType;
import com.destrostudios.icetea.core.render.shadow.ShadowConfig;
import com.destrostudios.icetea.core.render.shadow.ShadowMode;
import com.destrostudios.icetea.core.scene.*;
import com.destrostudios.icetea.core.scene.gui.Panel;
import com.destrostudios.icetea.core.shader.FileShader;
import com.destrostudios.icetea.core.texture.BufferedTexture;
import com.destrostudios.icetea.core.texture.Texture;
import com.destrostudios.icetea.imgui.ImGuiSystem;
import com.destrostudios.icetea.samples.filter.RadialBlurFilter;
import com.destrostudios.icetea.samples.filter.SepiaFilter;
import com.destrostudios.icetea.samples.terrain.GrassConfig;
import com.destrostudios.icetea.samples.terrain.GrassFactory;
import com.destrostudios.icetea.samples.water.*;
import imgui.ImGui;
import org.joml.*;

import java.lang.Math;
import java.util.ArrayList;
import java.util.LinkedList;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.vulkan.VK10.*;

public class TestApplication extends Application {

    public static void main(String[] args) {
        new TestApplication().start();
    }

    public TestApplication() {
        config.setEnableValidationLayer(true);
        config.setDisplayFpsInTitle(true);
    }
    private Material materialCool;
    private Panel panel1;
    private Node nodeRotating;
    private Geometry geometryWater;
    private Geometry geometryGround;
    private Geometry geometryGrass;
    private Material materialGrass;
    private Geometry geometryChalet3;
    private Node nodeDennis;
    private Node animatedObject2;
    private int animatedObject2AnimationIndex;
    private Node nodeDuck;
    private Geometry geometryBounds;
    private Node nodeCollisions;
    private BitmapText bitmapTextDynamic;
    private boolean hasAddedDennis;
    private boolean hasRemovedDennis;
    private boolean rotateObjects = true;
    private RadialBlurFilter radialBlurFilter;
    private SepiaFilter sepiaFilter;

    @Override
    protected void init() {
        super.init();
        assetManager.addLocator(new FileLocator("./assets"));

        sceneCamera.setLocation(new Vector3f(0, 0.3f, 5));

        DirectionalLight directionalLight = new DirectionalLight();
        directionalLight.setDirection(new Vector3f(-1, -1, -1).normalize());
        directionalLight.enableShadows(new ShadowConfig());
        setLight(directionalLight);

        SpotLight spotLight = new SpotLight();
        spotLight.setTranslation(new Vector3f(-2, 3.25f, 2.5f));
        spotLight.setRotation(new Quaternionf().rotateLocalX((float) (-0.3333f * Math.PI)));
        spotLight.enableShadows(new ShadowConfig());
        // setLight(spotLight);

        sceneNode.setAffectedByLight(true);

        nodeRotating = new Node();
        sceneNode.add(nodeRotating);

        materialCool = new Material();
        materialCool.setVertexShader(new FileShader("shaders/veryCool.vert"));
        materialCool.setFragmentShader(new FileShader("shaders/veryCool.frag"));
        materialCool.setTransparent(true);

        // GUI

        panel1 = new Panel();
        panel1.move(new Vector3f(50, 50, 0));
        panel1.scale(new Vector3f(200, 140, 0));
        panel1.setBackground(assetManager.loadTexture("textures/icetea1.png"));
        guiNode.add(panel1);

        Panel panel2 = new Panel();
        panel2.move(new Vector3f(75, 75, 1));
        panel2.scale(new Vector3f(200, 130, 0));
        panel2.setBackground(assetManager.loadTexture("textures/icetea2.png"));
        guiNode.add(panel2);

        BitmapFont bitmapFont = assetManager.loadBitmapFont("com/destrostudios/icetea/core/fonts/Verdana_18.fnt");

        BitmapText bitmapTextStatic = new BitmapText(bitmapFont, "Hello World.");
        bitmapTextStatic.move(new Vector3f(-0.5f, 0.1f, 3));
        bitmapTextStatic.scale(new Vector3f(0.005f, -0.005f, 0.005f));
        bitmapTextStatic.setRenderBucket(RenderBucketType.TRANSPARENT);
        bitmapTextStatic.getMaterial().setCullMode(VK_CULL_MODE_NONE);
        sceneNode.add(bitmapTextStatic);

        bitmapTextDynamic = new BitmapText(bitmapFont);
        bitmapTextDynamic.move(new Vector3f(325, 75, 1));
        guiNode.add(bitmapTextDynamic);

        // Water

        float waterSize = 100;
        geometryWater = WaterFactory.createWater(new WaterConfig());
        geometryWater.move(new Vector3f(waterSize / -2, 0, waterSize / -2));
        geometryWater.scale(new Vector3f(waterSize, 1, waterSize));
        sceneNode.add(geometryWater);

        // Ground

        Quad meshGround = new Quad(10, 10);

        Material materialGround = new Material();
        materialGround.setDefaultShaders();
        materialGround.getParameters().setVector4f("color", new Vector4f(0.5f, 0.5f, 0.5f, 1));

        geometryGround = new Geometry();
        geometryGround.setMesh(meshGround);
        geometryGround.setMaterial(materialGround);
        geometryGround.move(new Vector3f(-5, -0.25f, 5));
        geometryGround.rotate(new Quaternionf(new AxisAngle4f((float) (Math.PI / -2), 1, 0, 0)));
        geometryGround.setShadowMode(ShadowMode.RECEIVE);

        // Grass

        geometryGrass = GrassFactory.createGrass(new GrassConfig(), assetManager);
        materialGrass = geometryGrass.getMaterial();
        geometryGrass.move(new Vector3f(-5, -0.25f, -5));
        geometryGrass.scale(new Vector3f(10, 10, 10));
        geometryGrass.setShadowMode(ShadowMode.RECEIVE);

        // Chalet

        Mesh meshChalet = assetManager.loadMesh("models/chalet.obj");
        meshChalet.generateNormals();
        meshChalet.loadCollisionTree(); // Preload to avoid lag later on

        Material materialChalet = new Material();
        materialChalet.setDefaultShaders();
        BufferedTexture textureChalet = assetManager.loadTexture("textures/chalet.jpg");
        materialChalet.setTexture("diffuseMap", textureChalet);

        Geometry geometryChalet1 = new Geometry();
        geometryChalet1.setMesh(meshChalet);
        geometryChalet1.setMaterial(materialChalet);
        geometryChalet1.setShadowMode(ShadowMode.CAST);
        geometryChalet1.rotate(new Quaternionf(new AxisAngle4f((float) (Math.PI / -2), 1, 0, 0)));

        Node nodeChalet1 = new Node();
        nodeChalet1.add(geometryChalet1);
        nodeRotating.add(nodeChalet1);

        Geometry geometryChalet2 = new Geometry();
        geometryChalet2.setMesh(meshChalet);
        geometryChalet2.setMaterial(materialChalet);
        geometryChalet2.scale(new Vector3f(0.5f, 0.5f, 1));
        geometryChalet2.rotate(new Quaternionf(new AxisAngle4f((float) (Math.PI / -2), 1, 0, 0)));
        geometryChalet2.setShadowMode(ShadowMode.CAST);
        sceneNode.add(geometryChalet2);

        Node nodeChalet2 = new Node();
        nodeChalet2.add(geometryChalet2);
        nodeChalet2.move(new Vector3f(1.5f, 0.25f, -1));
        nodeRotating.add(nodeChalet2);

        geometryChalet3 = new Geometry();
        geometryChalet3.setMesh(meshChalet);
        geometryChalet3.setMaterial(materialCool);
        geometryChalet3.scale(new Vector3f(0.5f, 0.5f, 1));
        geometryChalet3.rotate(new Quaternionf(new AxisAngle4f((float) (Math.PI / -2), 1, 0, 0)));
        geometryChalet3.setRenderBucket(RenderBucketType.TRANSPARENT);
        geometryChalet3.setShadowMode(ShadowMode.CAST);

        Node nodeChalet3 = new Node();
        nodeChalet3.add(geometryChalet3);
        nodeChalet3.move(new Vector3f(-1.5f, 0.5f, -1.1f));
        nodeRotating.add(nodeChalet3);

        // Trees

        Mesh meshTrees = assetManager.loadMesh("models/trees.obj");

        Material materialTrees = new Material();
        materialTrees.setDefaultShaders();
        materialTrees.setTexture("diffuseMap", assetManager.loadTexture("textures/trees.jpg"));
        materialTrees.getParameters().setVector4f("color", new Vector4f(0, 0, 1, 1));

        Geometry geometryTrees = new Geometry();
        geometryTrees.setMesh(meshTrees);
        geometryTrees.setMaterial(materialTrees);
        geometryTrees.rotate(new Quaternionf(new AxisAngle4f((float) (Math.PI / -2), 1, 0, 0)));
        geometryTrees.scale(new Vector3f(0.01f, 0.01f, 0.01f));
        geometryTrees.setShadowMode(ShadowMode.CAST_AND_RECEIVE);
        sceneNode.add(geometryTrees);

        Node nodeTrees = new Node();
        nodeTrees.add(geometryTrees);
        nodeTrees.move(new Vector3f(0, 0.25f, 1));
        nodeRotating.add(nodeTrees);

        // Dennis

        Mesh meshDennis = assetManager.loadMesh("models/dennis.obj");
        meshDennis.loadCollisionTree(); // Preload to avoid lag later on

        Material materialDennis = new Material();
        materialDennis.setDefaultShaders();
        Texture textureDennis = assetManager.loadTexture("textures/dennis.jpg");
        materialDennis.setTexture("diffuseMap", textureDennis);
        materialDennis.getParameters().setVector4f("color", new Vector4f(1, 1, 0, 1));

        Geometry geometryDennis = new Geometry();
        geometryDennis.setMesh(meshDennis);
        geometryDennis.setMaterial(materialDennis);
        geometryDennis.rotate(new Quaternionf(new AxisAngle4f((float) (Math.PI / -2), 1, 0, 0)));
        geometryDennis.scale(new Vector3f(0.005f, 0.005f, 0.005f));
        geometryDennis.setShadowMode(ShadowMode.CAST_AND_RECEIVE);

        nodeDennis = new Node();
        nodeDennis.add(geometryDennis);
        nodeDennis.move(new Vector3f(0, 0.25f, 1));

        // Duck

        nodeDuck = (Node) assetManager.loadModel("models/duck/Duck.gltf");
        nodeDuck.scale(new Vector3f(0.25f, 0.25f, 0.25f));
        nodeDuck.setShadowMode(ShadowMode.CAST);

        Node nodeDuckWrapper = new Node();
        nodeDuckWrapper.add(nodeDuck);
        nodeDuckWrapper.move(new Vector3f(1, -0.25f, 1.5f));
        nodeRotating.add(nodeDuckWrapper);

        // Animated objects

        Node animatedObject1 = (Node) assetManager.loadModel("models/simple_skin/SimpleSkin.gltf");
        animatedObject1.forEachGeometry(geometry -> geometry.getMesh().generateNormals());
        animatedObject1.move(new Vector3f(-2.5f, 0, 0));
        animatedObject1.scale(new Vector3f(0.5f, 0.5f, 0.5f));
        animatedObject1.setShadowMode(ShadowMode.CAST_AND_RECEIVE);
        AnimationControl animationControl1 = animatedObject1.getFirstControl(AnimationControl.class);
        animationControl1.play(0);
        sceneNode.add(animatedObject1);

        animatedObject2 = (Node) assetManager.loadModel("models/footman/scene.gltf", GltfLoaderSettings.builder().bakeGeometries(true).build());
        animatedObject2.move(new Vector3f(2.5f, 0, 0));
        animatedObject2.rotate(new Quaternionf(new AxisAngle4f((float) Math.PI, 1, 0, 0)));
        animatedObject2.rotate(new Quaternionf(new AxisAngle4f((float) (Math.PI / 2), 0, 1, 0)));
        animatedObject2.scale(new Vector3f(0.5f, 0.5f, 0.5f));
        animatedObject2.forEachGeometry(geometry -> geometry.getMaterial().setCullMode(VK_CULL_MODE_NONE));
        animatedObject2.setShadowMode(ShadowMode.CAST_AND_RECEIVE);
        AnimationControl animationControl2 = animatedObject2.getFirstControl(AnimationControl.class);
        animationControl2.play(animatedObject2AnimationIndex);
        sceneNode.add(animatedObject2);

        Spatial animatedObject3 = assetManager.loadModel("models/fallacia35.gltf", GltfLoaderSettings.builder().bakeGeometries(true).build());
        animatedObject3.move(new Vector3f(3, 0, 0));
        animatedObject3.rotate(new Quaternionf(new AxisAngle4f((float) (Math.PI / -2), 1, 0, 0)));
        animatedObject3.scale(new Vector3f(23, 23, 23));
        animatedObject3.setShadowMode(ShadowMode.CAST_AND_RECEIVE);
        AnimationControl animationControl3 = animatedObject3.getFirstControl(AnimationControl.class);
        animationControl3.play("Armature|mixamo.com|Layer0");
        animationControl3.setSpeed(2);
        sceneNode.add(animatedObject3);

        for (int i = 0; i < 2; i++) {
            Spatial chicken = assetManager.loadModel("models/chicken/chicken.gltf", GltfLoaderSettings.builder().bakeGeometries(i == 0).build());
            chicken.move(new Vector3f(2 + (i * 0.4f), -0.2f, 1));
            chicken.scale(new Vector3f(0.5f, 0.5f, 0.5f));
            chicken.setShadowMode(ShadowMode.CAST_AND_RECEIVE);
            AnimationControl chickenAnimationControl = chicken.getFirstControl(AnimationControl.class);
            chickenAnimationControl.play("Stand");
            sceneNode.add(chicken);
        }

        // Sky

        Mesh meshSky = assetManager.loadMesh("models/dome.obj");

        Material materialSky = new Material();
        materialSky.setVertexShader(new FileShader("shaders/atmosphere.vert"));
        materialSky.setFragmentShader(new FileShader("shaders/atmosphere.frag"));
        materialSky.setCullMode(VK_CULL_MODE_FRONT_BIT);

        Geometry geometrySky = new Geometry();
        geometrySky.setMesh(meshSky);
        geometrySky.setMaterial(materialSky);
        geometrySky.scale(new Vector3f(50, 50, 50));
        geometrySky.setRenderBucket(RenderBucketType.BACKGROUND);
        sceneNode.add(geometrySky);

        // Knot

        Mesh meshKnot = assetManager.loadMesh("models/knot.obj");

        Material materialKnot = new Material();
        materialKnot.setDefaultShaders();
        materialKnot.setTexture("diffuseMap", textureChalet);
        materialKnot.getParameters().setVector4f("color", new Vector4f(0.33f, 1.33f, 0.33f, 1));

        Geometry geometryKnot = new Geometry();
        geometryKnot.setMesh(meshKnot);
        geometryKnot.setMaterial(materialKnot);
        geometryKnot.rotate(new Quaternionf(new AxisAngle4f((float) (Math.PI / -2), 1, 0, 0)));
        geometryKnot.scale(new Vector3f(0.01f, 0.01f, 0.01f));
        geometryKnot.setShadowMode(ShadowMode.CAST_AND_RECEIVE);

        Node nodeKnot = new Node();
        nodeKnot.move(new Vector3f(-1.5f, 0.75f, 0.2f));
        nodeKnot.add(geometryKnot);
        nodeRotating.add(nodeKnot);

        // Bounds

        Mesh meshBox = new Box(1, 1, 1);

        Material materialBounds = new Material();
        materialBounds.setDefaultShaders();
        materialBounds.setCullMode(VK_CULL_MODE_NONE);
        materialBounds.setFillMode(VK_POLYGON_MODE_LINE);
        materialBounds.getParameters().setVector4f("color", new Vector4f(1, 0, 0, 1));

        geometryBounds = new Geometry();
        geometryBounds.setMesh(meshBox);
        geometryBounds.setMaterial(materialBounds);
        sceneNode.add(geometryBounds);

        // Collisions

        nodeCollisions = new Node();
        sceneNode.add(nodeCollisions);

        // Filters

        radialBlurFilter = new RadialBlurFilter();
        sepiaFilter = new SepiaFilter();

        // Inputs

        CameraMouseRotateSystem cameraMouseRotateSystem = new CameraMouseRotateSystem(sceneCamera);
        CameraKeyMoveSystem cameraKeyMoveSystem = new CameraKeyMoveSystem(sceneCamera);
        ImGuiSystem imGuiSystem = new ImGuiSystem(ImGui::showDemoWindow);
        inputManager.addKeyListener(keyEvent -> {
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
                case GLFW_KEY_3:
                    if (keyEvent.getAction() == GLFW_PRESS) {
                        if (geometryWater.getParent() == sceneNode) {
                            sceneNode.remove(geometryWater);
                        } else {
                            sceneNode.add(geometryWater);
                        }
                    }
                    break;
                case GLFW_KEY_4:
                    if (keyEvent.getAction() == GLFW_PRESS) {
                        if (geometryGround.getParent() == sceneNode) {
                            sceneNode.remove(geometryGround);
                        } else {
                            sceneNode.add(geometryGround);
                        }
                    }
                    break;
                case GLFW_KEY_5:
                    if (keyEvent.getAction() == GLFW_PRESS) {
                        animatedObject2AnimationIndex = ((animatedObject2AnimationIndex + 1) % animationControl2.getAnimations().size());
                        animationControl2.play(animatedObject2AnimationIndex);
                    }
                    break;
                case GLFW_KEY_6:
                    if (keyEvent.getAction() == GLFW_PRESS) {
                        rotateObjects = !rotateObjects;
                    }
                    break;
                case GLFW_KEY_7:
                    if (keyEvent.getAction() == GLFW_PRESS) {
                        nodeDuck.setShadowMode((nodeDuck.getShadowMode() == ShadowMode.INHERIT) ? ShadowMode.CAST : ShadowMode.INHERIT);
                    }
                    break;
                case GLFW_KEY_8:
                    if (keyEvent.getAction() == GLFW_PRESS) {
                        if (geometryGrass.getParent() == sceneNode) {
                            sceneNode.remove(geometryGrass);
                        } else {
                            sceneNode.add(geometryGrass);
                        }
                    }
                    break;
                case GLFW_KEY_9:
                    if (keyEvent.getAction() == GLFW_PRESS) {
                        if (hasSystem(cameraMouseRotateSystem)) {
                            removeSystem(cameraMouseRotateSystem);
                            removeSystem(cameraKeyMoveSystem);
                        } else {
                            addSystem(cameraMouseRotateSystem);
                            addSystem(cameraKeyMoveSystem);
                        }
                    }
                    break;
                case GLFW_KEY_0:
                    if (keyEvent.getAction() == GLFW_PRESS) {
                        for (int i = 0; i < 10; i++) {
                            Node clone = nodeDuckWrapper.clone(CloneContext.reuseAll());
                            float deltaX = (float) ((Math.random() * 2) - 1);
                            float deltaZ = (float) ((Math.random() * 2) - 1);
                            clone.getLocalTransform().getTranslation().add(deltaX, 0, deltaZ);
                            nodeRotating.add(clone);
                        }
                    }
                    break;
                case GLFW_KEY_G:
                    if (keyEvent.getAction() == GLFW_PRESS) {
                        if (hasSystem(imGuiSystem)) {
                            removeSystem(imGuiSystem);
                        } else {
                            addSystem(imGuiSystem);
                        }
                    }
                    break;
                case GLFW_KEY_DELETE:
                    if (keyEvent.getAction() == GLFW_PRESS) {
                        cleanupNativeState();
                    }
                    break;
                case GLFW_KEY_F12:
                    if (keyEvent.getAction() == GLFW_PRESS) {
                        stop();
                    }
                    break;
            }
        });
        inputManager.addMouseButtonListener(mouseButtonEvent -> {
            if (mouseButtonEvent.getAction() == GLFW_PRESS) {
                Vector3f worldCoordinatesFront = getWorldCoordinates(inputManager.getCursorPosition(), 0);
                Vector3f worldCoordinatesBack = getWorldCoordinates(inputManager.getCursorPosition(), 1);
                Vector3f rayDirection = worldCoordinatesBack.sub(worldCoordinatesFront, new Vector3f());
                Ray ray = new Ray(worldCoordinatesFront, rayDirection);

                ArrayList<CollisionResult> collisionResults = new ArrayList<>();
                if (mouseButtonEvent.getButton() == GLFW_MOUSE_BUTTON_LEFT) {
                    sceneNode.collideStatic(ray, collisionResults);
                } else {
                    sceneNode.collideDynamic(ray, collisionResults);
                }

                LinkedList<Geometry> displayedCollisions = new LinkedList<>();
                for (CollisionResult collisionResult : collisionResults) {
                    if (collisionResult.getGeometry().getParent() != nodeCollisions) {
                        Geometry geometryBox = new Geometry();
                        geometryBox.setMesh(meshBox);

                        Material materialBox = new Material();
                        materialBox.setDefaultShaders();
                        geometryBox.setMaterial(materialBox);

                        float boxSize = 0.05f;
                        geometryBox.setLocalTranslation(collisionResult.getPosition().sub((boxSize / 2), (boxSize / 2), (boxSize / 2), new Vector3f()));
                        geometryBox.setLocalScale(new Vector3f(boxSize, boxSize, boxSize));
                        displayedCollisions.add(geometryBox);
                    }
                }

                nodeCollisions.removeAll();
                for (Geometry displayedCollision : displayedCollisions) {
                    nodeCollisions.add(displayedCollision);
                }
            }
        });
        bitmapTextDynamic.setText("Hi");
        materialCool.getParameters().setFloat("time", time);
        materialGrass.getParameters().setFloat("time", time);
    }

    @Override
    protected void update(float tpf) {
        super.update(tpf);
        if ((time > 20) && (!hasAddedDennis)) {
            nodeRotating.add(nodeDennis);
            hasAddedDennis = true;
        } else if ((time > 24) && (!hasRemovedDennis)) {
            nodeRotating.remove(nodeDennis);
            nodeDennis.cleanupNativeState();
            hasRemovedDennis = true;
        }

        if (rotateObjects) {
            for (Spatial spatial : nodeRotating.getChildren()) {
                spatial.setLocalRotation(new Quaternionf(new AxisAngle4f((float) (time * (Math.PI / 2)), 0, 1, 0)));
            }
            panel1.setLocalRotation(new Quaternionf(new AxisAngle4f((float) (time * (Math.PI / 2)), 0, 0, 1)));
        }

        BoundingBox debugWorldBounds = geometryChalet3.getWorldBounds();
        geometryBounds.setLocalTranslation(debugWorldBounds.getCenter().sub(debugWorldBounds.getExtent(), new Vector3f()));
        geometryBounds.setLocalScale(debugWorldBounds.getExtent().mul(2, new Vector3f()));

        bitmapTextDynamic.setLocalTranslation(getScreenCoordinates(animatedObject2.getWorldTransform().getTranslation().add(new Vector3f(0, 1, 0), new Vector3f())));
        bitmapTextDynamic.setText("Time:\n" + time);
        materialCool.getParameters().setFloat("time", time);
        materialGrass.getParameters().setFloat("time", time);
    }

    @Override
    protected void cleanup() {
        super.cleanup();
        radialBlurFilter.getFilterRenderJob().cleanupNative();
        sepiaFilter.getFilterRenderJob().cleanupNative();
        geometryGrass.cleanupNativeState();
        geometryGround.cleanupNativeState();
        geometryWater.cleanupNativeState();
    }
}
