package com.destrostudios.icetea.test;

import com.destrostudios.icetea.core.*;
import com.destrostudios.icetea.core.data.VertexData;
import com.destrostudios.icetea.core.filter.*;
import com.destrostudios.icetea.core.light.*;
import com.destrostudios.icetea.core.material.Material;
import com.destrostudios.icetea.core.mesh.*;
import com.destrostudios.icetea.core.model.GltfModelLoader;
import com.destrostudios.icetea.core.scene.*;
import com.destrostudios.icetea.core.shader.Shader;
import com.destrostudios.icetea.core.water.*;
import org.joml.*;

import java.lang.Math;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.vulkan.VK10.VK_CULL_MODE_FRONT_BIT;

public class TestApplication extends Application {

    public static void main(String[] args) {
        new TestApplication().start();
    }

    private Material materialCool;
    private Geometry geometryWater;
    private Geometry geometryGround;
    private Geometry geometryDennis;
    private Node nodeSkyWrapper;
    private boolean hasAddedDennis;
    private boolean hasRemovedDennis;
    private Vector3f cameraMoveDirection = new Vector3f();

    @Override
    protected void initScene() {
        camera.setLocation(new Vector3f(0, -5, 0.3f));
        camera.setRotation(new Vector3f(-88, 0, 0));

        DirectionalLight directionalLight = new DirectionalLight();
        directionalLight.setDirection(new Vector3f(1, 0, -0.25f).normalize());
        directionalLight.addAffectedSpatial(rootNode);
        directionalLight.addShadows(2048);
        setLight(directionalLight);

        SpotLight spotLight = new SpotLight();
        spotLight.setTranslation(new Vector3f(-2, -2.5f, 3.25f));
        spotLight.setRotation(new Vector3f(-60, 0, 0));
        spotLight.addAffectedSpatial(rootNode);
        spotLight.addShadows(2048);
        // setLight(spotLight);

        Shader vertexShaderDefault = new Shader("shaders/my_shader.vert", new String[] { "light", "shadow" });
        Shader fragShaderDefault = new Shader("shaders/my_shader.frag", new String[] { "light", "shadow" });

        Shader vertexShaderCool = new Shader("shaders/my_cool_shader.vert");
        Shader fragShaderCool = new Shader("shaders/my_cool_shader.frag", new String[] { "texCoordColor", "alphaPulsate" });

        materialCool = new Material();
        materialCool.setVertexShader(vertexShaderCool);
        materialCool.setFragmentShader(fragShaderCool);
        materialCool.setTransparent(true);

        // Water

        float waterSize = 100;
        geometryWater = WaterFactory.createWater(new WaterConfig());
        geometryWater.move(new Vector3f(waterSize / -2, waterSize / -2, 0));
        geometryWater.scale(new Vector3f(waterSize, waterSize, 1));
        rootNode.add(geometryWater);

        // Ground

        Quad meshGround = new Quad(10, 10);

        Material materialGround = new Material();
        materialGround.setVertexShader(vertexShaderDefault);
        materialGround.setFragmentShader(fragShaderDefault);
        materialGround.getParameters().setVector4f("color", new Vector4f(1, 1, 1, 1));

        geometryGround = new Geometry();
        geometryGround.setMesh(meshGround);
        geometryGround.setMaterial(materialGround);
        geometryGround.move(new Vector3f(0, 0, -0.25f));

        // Chalet

        Mesh meshChalet = new Mesh();
        meshChalet.loadObjModel("models/chalet.obj");
        meshChalet.generateNormals();

        Material materialChalet = new Material();
        materialChalet.setVertexShader(vertexShaderDefault);
        materialChalet.setFragmentShader(fragShaderDefault);
        Texture textureChalet = new FileTexture("textures/chalet.jpg");
        materialChalet.setTexture("diffuseMap", textureChalet);

        Geometry geometryChalet1 = new Geometry();
        geometryChalet1.setMesh(meshChalet);
        geometryChalet1.setMaterial(materialChalet);
        rootNode.add(geometryChalet1);

        Geometry geometryChalet2 = new Geometry();
        geometryChalet2.setMesh(meshChalet);
        geometryChalet2.setMaterial(materialChalet);
        geometryChalet2.move(new Vector3f(1.5f, 1, 0.25f));
        geometryChalet2.rotate(new Quaternionf(new AxisAngle4f((float) Math.toRadians(45), 0, 0, 1)));
        geometryChalet2.scale(new Vector3f(0.5f, 0.5f, 1));
        rootNode.add(geometryChalet2);

        Geometry geometryChalet3 = new Geometry();
        geometryChalet3.setMesh(meshChalet);
        geometryChalet3.setMaterial(materialCool);
        geometryChalet3.move(new Vector3f(-0.3f, 0.3f, 0.25f));
        geometryChalet3.scale(new Vector3f(0.5f, 0.5f, 1));

        Node nodeChalet3 = new Node();
        nodeChalet3.add(geometryChalet3);
        nodeChalet3.move(new Vector3f(-1.2f, 0.8f, 0.25f));
        nodeChalet3.rotate(new Quaternionf(new AxisAngle4f((float) Math.toRadians(-45), 0, 1, 0)));
        rootNode.add(nodeChalet3);

        // Trees

        Mesh meshTrees = new Mesh();
        meshTrees.loadObjModel("models/trees.obj");

        Material materialTrees = new Material();
        materialTrees.setVertexShader(vertexShaderDefault);
        materialTrees.setFragmentShader(fragShaderDefault);
        Texture textureTree = new FileTexture("textures/trees.jpg");
        materialTrees.setTexture("diffuseMap", textureTree);
        materialTrees.getParameters().setVector4f("color", new Vector4f(0, 0, 1, 1));

        Geometry geometryTrees = new Geometry();
        geometryTrees.setMesh(meshTrees);
        geometryTrees.setMaterial(materialTrees);
        geometryTrees.move(new Vector3f(0, -1, 0.25f));
        geometryTrees.scale(new Vector3f(0.01f, 0.01f, 0.01f));
        rootNode.add(geometryTrees);

        // Dennis

        Mesh meshDennis = new Mesh();
        meshDennis.loadObjModel("models/dennis.obj");

        Material materialDennis = new Material();
        materialDennis.setVertexShader(vertexShaderDefault);
        materialDennis.setFragmentShader(fragShaderDefault);
        materialDennis.setTexture("diffuseMap", new FileTexture("textures/dennis.jpg"));
        materialDennis.getParameters().setVector4f("color", new Vector4f(1, 1, 0, 1));

        geometryDennis = new Geometry();
        geometryDennis.setMesh(meshDennis);
        geometryDennis.setMaterial(materialDennis);
        geometryDennis.move(new Vector3f(0, -1, 0.25f));
        geometryDennis.scale(new Vector3f(0.005f, 0.005f, 0.005f));

        // Duck

        Node nodeDuck = new GltfModelLoader("models/duck.gltf").load();
        nodeDuck.setLocalRotation(new Quaternionf(new AxisAngle4f((float) Math.toRadians(-90), 1, 0, 0)));
        nodeDuck.forEachGeometry(geometry -> geometry.getMaterial().getParameters().setVector4f("color", new Vector4f(1, 0, 0, 1)));

        Node nodeDuckWrapper = new Node();
        nodeDuckWrapper.add(nodeDuck);
        nodeDuckWrapper.move(new Vector3f(1, -1.5f, -0.25f));
        nodeDuckWrapper.scale(new Vector3f(0.25f, 0.25f, 0.25f));
        rootNode.add(nodeDuckWrapper);

        // Sky

        Mesh meshSky = new Mesh();
        meshSky.loadObjModel("models/dome.obj");
        for (int i = 0; i< meshSky.getVertices().length; i++) {
            VertexData vertex = meshSky.getVertices()[i];
            Vector3f position = vertex.getVector3f("modelSpaceVertexPosition");
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

        nodeSkyWrapper = new Node();
        nodeSkyWrapper.add(geometrySky);
        nodeSkyWrapper.scale(new Vector3f(0.5f * camera.getZFar(), 0.5f * camera.getZFar(), 0.5f * camera.getZFar()));
        rootNode.add(nodeSkyWrapper);

        // Knot

        Mesh meshKnot = new Mesh();
        meshKnot.loadObjModel("models/knot.obj");

        Material materialKnot = new Material();
        materialKnot.setVertexShader(vertexShaderDefault);
        materialKnot.setFragmentShader(fragShaderDefault);
        Texture textureKnot = new FileTexture("textures/chalet.jpg");
        materialKnot.setTexture("diffuseMap", textureKnot);
        materialKnot.getParameters().setVector4f("color", new Vector4f(0, 1, 0, 1));

        Geometry geometryKnot = new Geometry();
        geometryKnot.setMesh(meshKnot);
        geometryKnot.setMaterial(materialKnot);
        geometryKnot.move(new Vector3f(-1.5f, -0.2f, 0.75f));
        geometryKnot.scale(new Vector3f(0.01f, 0.01f, 0.01f));
        rootNode.add(geometryKnot);

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
                case GLFW_KEY_3:
                    if (keyEvent.getAction() == GLFW_PRESS) {
                        if (geometryWater.getParent() == rootNode) {
                            rootNode.remove(geometryWater);
                        } else {
                            rootNode.add(geometryWater);
                        }
                    }
                    break;
                case GLFW_KEY_4:
                    if (keyEvent.getAction() == GLFW_PRESS) {
                        if (geometryGround.getParent() == rootNode) {
                            rootNode.remove(geometryGround);
                        } else {
                            rootNode.add(geometryGround);
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
    }

    @Override
    protected void update(float tpf) {
        if ((time > 8) && (!hasAddedDennis)) {
            rootNode.add(geometryDennis);
            hasAddedDennis = true;
        } else if ((time > 12) && (!hasRemovedDennis)) {
            rootNode.remove(geometryDennis);
            hasRemovedDennis = true;
        }
        for (Spatial spatial : rootNode.getChildren()) {
            if ((spatial != geometryWater) && (spatial != geometryGround) && (spatial != nodeSkyWrapper)) {
                spatial.setLocalRotation(new Quaternionf(new AxisAngle4f((float) (time * Math.toRadians(90)), 0.0f, 0.0f, 1.0f)));
            }
        }
        materialCool.getParameters().setFloat("time", time);
        camera.setLocation(camera.getLocation().add(cameraMoveDirection.mul(tpf * 3, new Vector3f())));
    }
}
