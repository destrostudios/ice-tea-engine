package com.destrostudios.icetea.test;

import com.destrostudios.icetea.core.*;
import com.destrostudios.icetea.core.animation.AnimationControl;
import com.destrostudios.icetea.core.asset.loader.GltfLoaderSettings;
import com.destrostudios.icetea.core.collision.BoundingBox;
import com.destrostudios.icetea.core.collision.CollisionResult;
import com.destrostudios.icetea.core.collision.Ray;
import com.destrostudios.icetea.core.data.VertexData;
import com.destrostudios.icetea.core.filter.*;
import com.destrostudios.icetea.core.light.*;
import com.destrostudios.icetea.core.material.Material;
import com.destrostudios.icetea.core.mesh.*;
import com.destrostudios.icetea.core.render.bucket.RenderBucketType;
import com.destrostudios.icetea.core.render.shadow.ShadowMode;
import com.destrostudios.icetea.core.scene.*;
import com.destrostudios.icetea.core.scene.gui.Panel;
import com.destrostudios.icetea.core.shader.Shader;
import com.destrostudios.icetea.core.terrain.GrassFactory;
import com.destrostudios.icetea.core.texture.Texture;
import com.destrostudios.icetea.core.water.*;
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

    private Material materialCool;
    private Panel panel1;
    private Geometry geometryWater;
    private Geometry geometryGround;
    private Geometry geometryGrass;
    private Material materialGrass;
    private Geometry geometryChalet1;
    private Geometry geometryChalet2;
    private Geometry geometryChalet3;
    private Geometry geometryDennis;
    private Spatial animatedObject1;
    private Spatial animatedObject2;
    private int animatedObject2AnimationIndex;
    private Node nodeSkyWrapper;
    private Node nodeDuck;
    private Geometry geometryKnot;
    private Geometry geometryBounds;
    private Node nodeCollisions;
    private boolean hasAddedDennis;
    private boolean hasRemovedDennis;
    private boolean rotateObjects = true;
    private Vector3f cameraMoveDirection = new Vector3f();

    @Override
    protected void initScene() {
        sceneCamera.setLocation(new Vector3f(0, -5, 0.3f));
        sceneCamera.setRotation(new Vector3f(-88, 0, 0));

        DirectionalLight directionalLight = new DirectionalLight();
        directionalLight.setDirection(new Vector3f(-1, 1, -1).normalize());
        directionalLight.addAffectedSpatial(sceneNode);
        directionalLight.addShadows(4096);
        setLight(directionalLight);

        SpotLight spotLight = new SpotLight();
        spotLight.setTranslation(new Vector3f(-2, -2.5f, 3.25f));
        spotLight.setRotation(new Vector3f(-60, 0, 0));
        spotLight.addAffectedSpatial(sceneNode);
        spotLight.addShadows(4096);
        // setLight(spotLight);

        Shader vertexShaderDefault = new Shader("shaders/my_shader.vert", new String[] { "light", "shadow" });
        Shader fragShaderDefault = new Shader("shaders/my_shader.frag", new String[] { "light", "shadow" });

        Shader vertexShaderCool = new Shader("shaders/my_cool_shader.vert");
        Shader fragShaderCool = new Shader("shaders/my_cool_shader.frag", new String[] { "texCoordColor", "alphaPulsate" });

        materialCool = new Material();
        materialCool.setVertexShader(vertexShaderCool);
        materialCool.setFragmentShader(fragShaderCool);
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

        // Water

        float waterSize = 100;
        geometryWater = WaterFactory.createWater(new WaterConfig());
        geometryWater.move(new Vector3f(waterSize / -2, waterSize / -2, 0));
        geometryWater.scale(new Vector3f(waterSize, waterSize, 1));
        sceneNode.add(geometryWater);

        // Ground

        Quad meshGround = new Quad(10, 10);

        Material materialGround = new Material();
        materialGround.setVertexShader(vertexShaderDefault);
        materialGround.setFragmentShader(fragShaderDefault);
        materialGround.getParameters().setVector4f("color", new Vector4f(1, 1, 1, 1));

        geometryGround = new Geometry();
        geometryGround.setMesh(meshGround);
        geometryGround.setMaterial(materialGround);
        geometryGround.move(new Vector3f(-5, -5, -0.25f));
        geometryGround.setShadowMode(ShadowMode.RECEIVE);

        // Grass

        geometryGrass = GrassFactory.createGrass(10, assetManager);
        materialGrass = geometryGrass.getMaterial();
        geometryGrass.move(new Vector3f(-5, -5, -0.25f));
        geometryGrass.scale(new Vector3f(10, 10, 10));
        geometryGrass.setShadowMode(ShadowMode.RECEIVE);

        // Chalet

        Mesh meshChalet = assetManager.loadMesh("models/chalet.obj");
        meshChalet.generateNormals();
        meshChalet.loadCollisionTree(); // Preload to avoid lag later on

        Material materialChalet = new Material();
        materialChalet.setVertexShader(vertexShaderDefault);
        materialChalet.setFragmentShader(fragShaderDefault);
        Texture textureChalet = assetManager.loadTexture("textures/chalet.jpg");
        materialChalet.setTexture("diffuseMap", textureChalet);

        geometryChalet1 = new Geometry();
        geometryChalet1.setMesh(meshChalet);
        geometryChalet1.setMaterial(materialChalet);
        geometryChalet1.setShadowMode(ShadowMode.CAST);
        sceneNode.add(geometryChalet1);

        geometryChalet2 = new Geometry();
        geometryChalet2.setMesh(meshChalet);
        geometryChalet2.setMaterial(materialChalet);
        geometryChalet2.move(new Vector3f(1.5f, 1, 0.25f));
        geometryChalet2.rotate(new Quaternionf(new AxisAngle4f((float) Math.toRadians(45), 0, 0, 1)));
        geometryChalet2.scale(new Vector3f(0.5f, 0.5f, 1));
        geometryChalet2.setShadowMode(ShadowMode.CAST);
        sceneNode.add(geometryChalet2);

        geometryChalet3 = new Geometry();
        geometryChalet3.setMesh(meshChalet);
        geometryChalet3.setMaterial(materialCool);
        geometryChalet3.move(new Vector3f(-0.3f, 0.3f, 0.25f));
        geometryChalet3.scale(new Vector3f(0.5f, 0.5f, 1));
        geometryChalet3.setRenderBucket(RenderBucketType.TRANSPARENT);
        geometryChalet3.setShadowMode(ShadowMode.CAST);

        Node nodeChalet3 = new Node();
        nodeChalet3.add(geometryChalet3);
        nodeChalet3.move(new Vector3f(-1.2f, 0.8f, 0.25f));
        nodeChalet3.rotate(new Quaternionf(new AxisAngle4f((float) Math.toRadians(-45), 0, 1, 0)));
        sceneNode.add(nodeChalet3);

        // Trees

        Mesh meshTrees = assetManager.loadMesh("models/trees.obj");

        Material materialTrees = new Material();
        materialTrees.setVertexShader(vertexShaderDefault);
        materialTrees.setFragmentShader(fragShaderDefault);
        Texture textureTree = assetManager.loadTexture("textures/trees.jpg");
        materialTrees.setTexture("diffuseMap", textureTree);
        materialTrees.getParameters().setVector4f("color", new Vector4f(0, 0, 1, 1));

        Geometry geometryTrees = new Geometry();
        geometryTrees.setMesh(meshTrees);
        geometryTrees.setMaterial(materialTrees);
        geometryTrees.move(new Vector3f(0, -1, 0.25f));
        geometryTrees.scale(new Vector3f(0.01f, 0.01f, 0.01f));
        geometryTrees.setShadowMode(ShadowMode.CAST_AND_RECEIVE);
        sceneNode.add(geometryTrees);

        // Dennis

        Mesh meshDennis = assetManager.loadMesh("models/dennis.obj");
        meshDennis.init(this); // Preload to avoid lag later on
        meshDennis.loadCollisionTree(); // Preload to avoid lag later on

        Material materialDennis = new Material();
        materialDennis.setVertexShader(vertexShaderDefault);
        materialDennis.setFragmentShader(fragShaderDefault);
        Texture textureDennis = assetManager.loadTexture("textures/dennis.jpg");
        textureDennis.init(this); // Preload to avoid lag later on
        materialDennis.setTexture("diffuseMap", textureDennis);
        materialDennis.getParameters().setVector4f("color", new Vector4f(1, 1, 0, 1));

        geometryDennis = new Geometry();
        geometryDennis.setMesh(meshDennis);
        geometryDennis.setMaterial(materialDennis);
        geometryDennis.move(new Vector3f(0, -1, 0.25f));
        geometryDennis.scale(new Vector3f(0.005f, 0.005f, 0.005f));
        geometryDennis.setShadowMode(ShadowMode.CAST_AND_RECEIVE);

        // Duck

        nodeDuck = (Node) assetManager.loadModel("models/duck/Duck.gltf");
        nodeDuck.setLocalRotation(new Quaternionf(new AxisAngle4f((float) Math.toRadians(-90), 1, 0, 0)));
        nodeDuck.forEachGeometry(geometry -> geometry.getMaterial().getParameters().setVector4f("color", new Vector4f(1, 0, 0, 1)));
        nodeDuck.setShadowMode(ShadowMode.CAST);

        Node nodeDuckWrapper = new Node();
        nodeDuckWrapper.add(nodeDuck);
        nodeDuckWrapper.move(new Vector3f(1, -1.5f, -0.25f));
        nodeDuckWrapper.scale(new Vector3f(0.25f, 0.25f, 0.25f));
        sceneNode.add(nodeDuckWrapper);

        // Animated objects

        animatedObject1 = assetManager.loadModel("models/simple_skin/SimpleSkin.gltf", GltfLoaderSettings.builder().generateNormals(true).build());
        animatedObject1.move(new Vector3f(-2.5f, 0, 0.6f));
        animatedObject1.rotate(new Quaternionf(new AxisAngle4f((float) Math.toRadians(90), 1, 0, 0)));
        animatedObject1.scale(new Vector3f(0.5f, 0.5f, 0.5f));
        animatedObject1.setShadowMode(ShadowMode.CAST_AND_RECEIVE);
        AnimationControl animationControl1 = (AnimationControl) animatedObject1.getControls().iterator().next();
        animationControl1.play(0);
        sceneNode.add(animatedObject1);

        animatedObject2 = assetManager.loadModel("models/footman/scene.gltf");
        animatedObject2.move(new Vector3f(2.5f, 0, 0));
        animatedObject2.rotate(new Quaternionf(new AxisAngle4f((float) Math.toRadians(180), 1, 0, 0)));
        animatedObject2.rotate(new Quaternionf(new AxisAngle4f((float) Math.toRadians(45), 0, 0, 1)));
        animatedObject2.scale(new Vector3f(0.5f, 0.5f, 0.5f));
        animatedObject2.setShadowMode(ShadowMode.CAST_AND_RECEIVE);
        AnimationControl animationControl2 = (AnimationControl) animatedObject2.getControls().iterator().next();
        animationControl2.play(animatedObject2AnimationIndex);
        sceneNode.add(animatedObject2);

        // Sky

        Mesh meshSky = assetManager.loadMesh("models/dome.obj");
        for (int i = 0; i< meshSky.getVertices().length; i++) {
            VertexData vertex = meshSky.getVertices()[i];
            Vector3f position = vertex.getVector3f("vertexPosition");
            vertex.setVector2f("vertexTexCoord", new Vector2f((position.x() + 1) * 0.5f, (position.z() + 1) * 0.5f));
        }

        Material materialSky = new Material();
        materialSky.setVertexShader(new Shader("shaders/atmosphere.vert"));
        materialSky.setFragmentShader(new Shader("shaders/atmosphere.frag"));
        materialSky.setCullMode(VK_CULL_MODE_FRONT_BIT);

        Geometry geometrySky = new Geometry();
        geometrySky.setMesh(meshSky);
        geometrySky.setMaterial(materialSky);
        geometrySky.setLocalRotation(new Quaternionf(new AxisAngle4f((float) Math.toRadians(90), 1, 0, 0)));
        geometrySky.setRenderBucket(RenderBucketType.BACKGROUND);

        nodeSkyWrapper = new Node();
        nodeSkyWrapper.add(geometrySky);
        nodeSkyWrapper.scale(new Vector3f(0.5f * sceneCamera.getZFar(), 0.5f * sceneCamera.getZFar(), 0.5f * sceneCamera.getZFar()));
        sceneNode.add(nodeSkyWrapper);

        // Knot

        Mesh meshKnot = assetManager.loadMesh("models/knot.obj");

        Material materialKnot = new Material();
        materialKnot.setVertexShader(vertexShaderDefault);
        materialKnot.setFragmentShader(fragShaderDefault);
        Texture textureKnot = assetManager.loadTexture("textures/chalet.jpg");
        materialKnot.setTexture("diffuseMap", textureKnot);
        materialKnot.getParameters().setVector4f("color", new Vector4f(0, 1, 0, 1));

        geometryKnot = new Geometry();
        geometryKnot.setMesh(meshKnot);
        geometryKnot.setMaterial(materialKnot);
        geometryKnot.move(new Vector3f(-1.5f, -0.2f, 0.75f));
        geometryKnot.scale(new Vector3f(0.01f, 0.01f, 0.01f));
        geometryKnot.setShadowMode(ShadowMode.CAST_AND_RECEIVE);
        sceneNode.add(geometryKnot);

        // Bounds

        Mesh meshBox = new Box(1, 1, 1);

        Material materialBounds = new Material();
        materialBounds.setVertexShader(vertexShaderDefault);
        materialBounds.setFragmentShader(fragShaderDefault);
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

        RadialBlurFilter radialBlurFilter = new RadialBlurFilter();
        SepiaFilter sepiaFilter = new SepiaFilter();

        // Inputs

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
                        animatedObject2AnimationIndex = ((animatedObject2AnimationIndex + 1) % animationControl2.getAnimations().length);
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
        inputManager.addMouseButtonListener(mouseButtonEvent -> {
            if (mouseButtonEvent.getAction() == GLFW_PRESS) {
                Vector3f worldCoordinatesFront = getWorldCoordinates(sceneCamera, inputManager.getCursorPosition(), 0);
                Vector3f worldCoordinatesBack = getWorldCoordinates(sceneCamera, inputManager.getCursorPosition(), 1);
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
                        materialBox.setVertexShader(vertexShaderDefault);
                        materialBox.setFragmentShader(fragShaderDefault);
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
    }

    @Override
    protected void update(float tpf) {
        if ((time > 8) && (!hasAddedDennis)) {
            sceneNode.add(geometryDennis);
            hasAddedDennis = true;
        } else if ((time > 12) && (!hasRemovedDennis)) {
            sceneNode.remove(geometryDennis);
            hasRemovedDennis = true;
        }

        if (rotateObjects) {
            for (Spatial spatial : sceneNode.getChildren()) {
                if ((spatial != geometryWater) && (spatial != geometryGround) && (spatial != geometryGrass) && (spatial != animatedObject1) && (spatial != animatedObject2) && (spatial != nodeSkyWrapper) && (spatial != geometryBounds) && (spatial != nodeCollisions)) {
                    updateTimeBasedRotation(spatial);
                }
            }
            updateTimeBasedRotation(panel1);
        }

        BoundingBox debugWorldBounds = geometryChalet3.getWorldBounds();
        geometryBounds.setLocalTranslation(debugWorldBounds.getCenter().sub(debugWorldBounds.getExtent(), new Vector3f()));
        geometryBounds.setLocalScale(debugWorldBounds.getExtent().mul(2, new Vector3f()));

        materialCool.getParameters().setFloat("time", time);
        materialGrass.getParameters().setFloat("time", time);

        sceneCamera.setLocation(sceneCamera.getLocation().add(cameraMoveDirection.mul(tpf * 3, new Vector3f())));
    }

    private void updateTimeBasedRotation(Spatial spatial) {
        spatial.setLocalRotation(new Quaternionf(new AxisAngle4f((float) (time * Math.toRadians(90)), 0.0f, 0.0f, 1.0f)));
    }
}
