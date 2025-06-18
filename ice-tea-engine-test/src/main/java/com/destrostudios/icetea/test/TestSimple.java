package com.destrostudios.icetea.test;

import com.destrostudios.icetea.core.Application;
import com.destrostudios.icetea.core.animation.AnimationControl;
import com.destrostudios.icetea.core.asset.loader.GltfLoaderSettings;
import com.destrostudios.icetea.core.asset.locator.FileLocator;
import com.destrostudios.icetea.core.camera.systems.CameraKeyMoveSystem;
import com.destrostudios.icetea.core.camera.systems.CameraMouseRotateSystem;
import com.destrostudios.icetea.core.clone.CloneContext;
import com.destrostudios.icetea.core.font.BitmapFont;
import com.destrostudios.icetea.core.font.BitmapText;
import com.destrostudios.icetea.core.light.DirectionalLight;
import com.destrostudios.icetea.core.material.Material;
import com.destrostudios.icetea.core.mesh.Quad;
import com.destrostudios.icetea.core.render.shadow.ShadowConfig;
import com.destrostudios.icetea.core.render.shadow.ShadowMode;
import com.destrostudios.icetea.core.scene.Geometry;
import com.destrostudios.icetea.core.scene.Spatial;
import com.destrostudios.icetea.core.shader.Shader;
import org.joml.AxisAngle4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.joml.Vector4f;

import java.util.HashMap;

import static org.lwjgl.glfw.GLFW.*;

public class TestSimple extends Application {

    public static void main(String[] args) {
        new TestSimple().start();
    }

    public TestSimple() {
        config.setEnableValidationLayer(true);
        config.setDisplayFpsInTitle(true);
    }
    private Geometry ground;
    private Geometry model;
    private BitmapText bitmapTextDynamic;
    private int cloneCounter;
    private HashMap<Integer, Float> cachedRandom = new HashMap<>();

    @Override
    protected void init() {
        super.init();
        assetManager.addLocator(new FileLocator("./assets"));

        sceneCamera.setLocation(new Vector3f(0, 1, 13));

        Shader vertexShaderDefault = new Shader("com/destrostudios/icetea/core/shaders/default.vert", new String[] {
            "com/destrostudios/icetea/core/shaders/nodes/light.glsllib",
            "com/destrostudios/icetea/core/shaders/nodes/shadow.glsllib"
        });
        Shader fragShaderDefault = new Shader("com/destrostudios/icetea/core/shaders/default.frag", new String[] {
            "com/destrostudios/icetea/core/shaders/nodes/light.glsllib",
            "com/destrostudios/icetea/core/shaders/nodes/shadow.glsllib"
        });

        Quad meshGround = new Quad(20, 20);

        Material materialGround = new Material();
        materialGround.setVertexShader(vertexShaderDefault);
        materialGround.setFragmentShader(fragShaderDefault);
        materialGround.getParameters().setVector4f("color", new Vector4f(1, 1, 1, 1));

        ground = new Geometry();
        ground.setMesh(meshGround);
        ground.setMaterial(materialGround);
        ground.move(new Vector3f(-10, -0.25f, 10));
        ground.rotate(new Quaternionf(new AxisAngle4f((float) (Math.PI / -2), 1, 0, 0)));
        ground.setShadowMode(ShadowMode.RECEIVE);
        sceneNode.add(ground);

        DirectionalLight directionalLight = new DirectionalLight();
        directionalLight.setDirection(new Vector3f(-1, -1, -1).normalize());
        directionalLight.enableShadows(new ShadowConfig());
        setLight(directionalLight);
        sceneNode.setAffectedByLight(true);

        BitmapFont bitmapFont = assetManager.loadBitmapFont("com/destrostudios/icetea/core/fonts/Verdana_18.fnt");
        bitmapTextDynamic = new BitmapText(bitmapFont);
        bitmapTextDynamic.move(new Vector3f(50, 50, 1));
        bitmapTextDynamic.setText("Hello");
        guiNode.add(bitmapTextDynamic);

        // Will of course only work if you have the model locally
        model = (Geometry) assetManager.loadModel("models/ghost/ghost.gltf", GltfLoaderSettings.builder().bakeGeometries(true).build());
        model.scale(new Vector3f(100, 100, 100));
        model.setShadowMode(ShadowMode.CAST_AND_RECEIVE);
        AnimationControl animationControl = model.getFirstControl(AnimationControl.class);
        animationControl.play("cast_spell");
        sceneNode.add(model);
        updateInternalState();
        sceneNode.remove(model);

        addSystem(new CameraMouseRotateSystem(sceneCamera));
        addSystem(new CameraKeyMoveSystem(sceneCamera));

        inputManager.addKeyListener(keyEvent -> {
            switch (keyEvent.getKey()) {
                case GLFW_KEY_0:
                    if (keyEvent.getAction() == GLFW_PRESS) {
                        cloneCounter++;
                        System.out.println("Counter: " + cloneCounter);
                        for (int i = 0; i < 100; i++) {
                            sceneNode.add(model.clone(CloneContext.reuseAll()));
                        }
                    }
                    break;
                case GLFW_KEY_DELETE:
                    if (keyEvent.getAction() == GLFW_PRESS) {
                        cleanupNativeState();
                        model.cleanupNativeState();
                    }
                    break;
            }
        });
    }

    @Override
    protected void update(float tpf) {
        super.update(tpf);
        int i = 0;
        for (Spatial child : sceneNode.getChildren()) {
            if (child != ground){
                float random = cachedRandom.computeIfAbsent(i, (j) -> (float) Math.random());
                float radius = random * 10;
                float progress = (time / 2) + i;
                float x = (float) (Math.sin(progress) * radius);
                float z = (float) (Math.cos(progress) * radius);
                child.setLocalTranslation(new Vector3f(x, 0, z));
                i++;
            }
        }
        bitmapTextDynamic.setText("" + time);
    }

    @Override
    protected void cleanup() {
        super.cleanup();
        model.cleanupNativeState();
    }
}
