package com.destrostudios.icetea.test;

import com.destrostudios.icetea.core.Application;
import com.destrostudios.icetea.core.data.VertexData;
import com.destrostudios.icetea.core.light.DirectionalLight;
import com.destrostudios.icetea.core.material.Material;
import com.destrostudios.icetea.core.mesh.Mesh;
import com.destrostudios.icetea.core.render.bucket.RenderBucketType;
import com.destrostudios.icetea.core.render.shadow.ShadowMode;
import com.destrostudios.icetea.core.scene.Geometry;
import com.destrostudios.icetea.core.scene.Node;
import com.destrostudios.icetea.core.shader.Shader;
import com.destrostudios.icetea.core.terrain.TerrainFactory;
import com.destrostudios.icetea.core.water.WaterConfig;
import com.destrostudios.icetea.core.water.WaterFactory;
import org.joml.AxisAngle4f;
import org.joml.Quaternionf;
import org.joml.Vector2f;
import org.joml.Vector3f;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.vulkan.VK10.VK_CULL_MODE_FRONT_BIT;

public class TestTerrain extends Application {

    public static void main(String[] args) {
        new TestTerrain().start();
    }

    private Vector3f cameraMoveDirection = new Vector3f();

    @Override
    protected void initScene() {
        sceneCamera.setLocation(new Vector3f(0, -50, 6));
        sceneCamera.setRotation(new Quaternionf().rotateLocalX((float) (-0.49f * Math.PI)));
        sceneCamera.setZFar(200);

        DirectionalLight directionalLight = new DirectionalLight();
        directionalLight.setDirection(new Vector3f(-1, 1, -1).normalize());
        directionalLight.addAffectedSpatial(sceneNode);
        directionalLight.addShadows(4096);
        setLight(directionalLight);

        // Terrain

        float terrainSize = 200;
        Geometry geometryTerrain = TerrainFactory.createTerrain(150);
        geometryTerrain.move(new Vector3f((terrainSize / -2), (terrainSize / -2), -0.2f));
        geometryTerrain.scale(new Vector3f(terrainSize, terrainSize, 40));
        geometryTerrain.setShadowMode(ShadowMode.CAST_AND_RECEIVE);
        sceneNode.add(geometryTerrain);

        // Water

        Geometry geometryWater = WaterFactory.createWater(new WaterConfig());
        geometryWater.move(new Vector3f(terrainSize / -2, terrainSize / -2, 4));
        geometryWater.scale(new Vector3f(terrainSize, terrainSize, 1));
        sceneNode.add(geometryWater);

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

        Node nodeSkyWrapper = new Node();
        nodeSkyWrapper.add(geometrySky);
        nodeSkyWrapper.scale(new Vector3f(0.5f * sceneCamera.getZFar(), 0.5f * sceneCamera.getZFar(), 0.5f * sceneCamera.getZFar()));
        sceneNode.add(nodeSkyWrapper);

        // Inputs

        inputManager.addKeyListener(keyEvent -> {
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
        sceneCamera.setLocation(sceneCamera.getLocation().add(cameraMoveDirection.mul(tpf * 30, new Vector3f())));
    }
}
