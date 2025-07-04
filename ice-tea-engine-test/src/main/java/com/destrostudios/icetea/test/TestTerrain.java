package com.destrostudios.icetea.test;

import com.destrostudios.icetea.core.Application;
import com.destrostudios.icetea.core.asset.locator.FileLocator;
import com.destrostudios.icetea.core.camera.systems.CameraKeyMoveSystem;
import com.destrostudios.icetea.core.camera.systems.CameraMouseRotateSystem;
import com.destrostudios.icetea.core.data.VertexData;
import com.destrostudios.icetea.core.light.DirectionalLight;
import com.destrostudios.icetea.core.material.Material;
import com.destrostudios.icetea.core.mesh.Mesh;
import com.destrostudios.icetea.core.render.bucket.RenderBucketType;
import com.destrostudios.icetea.core.render.shadow.ShadowConfig;
import com.destrostudios.icetea.core.render.shadow.ShadowMode;
import com.destrostudios.icetea.core.scene.Geometry;
import com.destrostudios.icetea.core.shader.FileShader;
import com.destrostudios.icetea.samples.terrain.TerrainFactory;
import com.destrostudios.icetea.samples.water.WaterConfig;
import com.destrostudios.icetea.samples.water.WaterFactory;
import org.joml.Vector2f;
import org.joml.Vector3f;

import static org.lwjgl.vulkan.VK10.VK_CULL_MODE_FRONT_BIT;

public class TestTerrain extends Application {

    public static void main(String[] args) {
        new TestTerrain().start();
    }

    public TestTerrain() {
        config.setEnableValidationLayer(true);
        config.setDisplayFpsInTitle(true);
    }

    @Override
    protected void init() {
        super.init();
        assetManager.addLocator(new FileLocator("./assets"));

        sceneCamera.setLocation(new Vector3f(0, 6, 50));
        sceneCamera.setZFar(200);

        DirectionalLight directionalLight = new DirectionalLight();
        directionalLight.setDirection(new Vector3f(-1, -1, -1).normalize());
        directionalLight.enableShadows(new ShadowConfig());
        setLight(directionalLight);
        sceneNode.setAffectedByLight(true);

        // Terrain

        float terrainSize = 200;
        Geometry geometryTerrain = TerrainFactory.createTerrain(150);
        geometryTerrain.move(new Vector3f((terrainSize / -2), -0.2f, (terrainSize / -2)));
        geometryTerrain.scale(new Vector3f(terrainSize, 40, terrainSize));
        geometryTerrain.setShadowMode(ShadowMode.CAST_AND_RECEIVE);
        sceneNode.add(geometryTerrain);

        // Water

        Geometry geometryWater = WaterFactory.createWater(new WaterConfig());
        geometryWater.move(new Vector3f(terrainSize / -2, 4, terrainSize / -2));
        geometryWater.scale(new Vector3f(terrainSize, 1, terrainSize));
        sceneNode.add(geometryWater);

        // Sky

        Mesh meshSky = assetManager.loadMesh("models/dome.obj");
        for (int i = 0; i< meshSky.getVertices().length; i++) {
            VertexData vertex = meshSky.getVertices()[i];
            Vector3f position = vertex.getVector3f("vertexPosition");
            vertex.setVector2f("vertexTexCoord", new Vector2f((position.x() + 1) * 0.5f, (position.z() + 1) * 0.5f));
        }

        Material materialSky = new Material();
        materialSky.setVertexShader(new FileShader("shaders/atmosphere.vert"));
        materialSky.setFragmentShader(new FileShader("shaders/atmosphere.frag"));
        materialSky.setCullMode(VK_CULL_MODE_FRONT_BIT);

        Geometry geometrySky = new Geometry();
        geometrySky.setMesh(meshSky);
        geometrySky.setMaterial(materialSky);
        geometrySky.scale(new Vector3f(100, 100, 100));
        geometrySky.setRenderBucket(RenderBucketType.BACKGROUND);
        sceneNode.add(geometrySky);

        addSystem(new CameraMouseRotateSystem(sceneCamera));

        CameraKeyMoveSystem cameraKeyMoveSystem = new CameraKeyMoveSystem(sceneCamera);
        cameraKeyMoveSystem.setMoveSpeed(30);
        addSystem(cameraKeyMoveSystem);
    }
}
