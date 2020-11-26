package com.destrostudios.icetea.test;

import com.destrostudios.icetea.core.*;
import com.destrostudios.icetea.core.filters.RadialBlurFilter;
import com.destrostudios.icetea.core.filters.SepiaFilter;
import com.destrostudios.icetea.core.lights.DirectionalLight;
import com.destrostudios.icetea.core.lights.SpotLight;
import com.destrostudios.icetea.core.meshes.Quad;
import org.joml.AxisAngle4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.joml.Vector4f;

import static org.lwjgl.glfw.GLFW.*;

public class TestApplication extends Application {

    private Material materialCool;
    private Geometry geometryGround;
    private Geometry geometryDennis;
    private boolean hasAddedDennis;
    private boolean hasRemovedDennis;
    private Vector3f cameraMoveDirection = new Vector3f();

    @Override
    protected void initScene() {
        camera.setLocation(new Vector3f(0, -5, 1.25f));
        camera.setRotation(new Vector3f(-80, 0, 0));

        DirectionalLight directionalLight = new DirectionalLight();
        directionalLight.setDirection(new Vector3f(1, 0, -0.25f).normalize());
        directionalLight.addAffectedSpatial(rootNode);
        directionalLight.addShadows(2048);
        setLight(directionalLight);

        SpotLight spotLight = new SpotLight();
        spotLight.setTranslation(new Vector3f(-2, -2.5f, 3));
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

        // Ground

        Quad meshGround = new Quad(10, 10);

        Material materialGround = new Material();
        materialGround.setVertexShader(vertexShaderDefault);
        materialGround.setFragmentShader(fragShaderDefault);
        materialGround.getParameters().setVector4f("color", new Vector4f(1, 1, 1, 1));

        geometryGround = new Geometry();
        geometryGround.setMesh(meshGround);
        geometryGround.setMaterial(materialGround);
        geometryGround.move(new Vector3f(0, 0, -0.5f));
        rootNode.add(geometryGround);

        // Chalet

        Mesh meshChalet = new Mesh();
        meshChalet.loadModel("models/chalet.obj");
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
        geometryChalet2.move(new Vector3f(1.5f, 1, 0));
        geometryChalet2.rotate(new Quaternionf(new AxisAngle4f((float) Math.toRadians(45), 0, 0, 1)));
        geometryChalet2.scale(new Vector3f(0.5f, 0.5f, 1));
        rootNode.add(geometryChalet2);

        Geometry geometryChalet3 = new Geometry();
        geometryChalet3.setMesh(meshChalet);
        geometryChalet3.setMaterial(materialCool);
        geometryChalet3.move(new Vector3f(-0.3f, 0.3f, 0));
        geometryChalet3.scale(new Vector3f(0.5f, 0.5f, 1));

        Node nodeChalet3 = new Node();
        nodeChalet3.add(geometryChalet3);
        nodeChalet3.move(new Vector3f(-1.2f, 0.8f, 0));
        nodeChalet3.rotate(new Quaternionf(new AxisAngle4f((float) Math.toRadians(-45), 0, 1, 0)));
        rootNode.add(nodeChalet3);

        // Trees

        Mesh meshTrees = new Mesh();
        meshTrees.loadModel("models/trees.obj");

        Material materialTrees = new Material();
        materialTrees.setVertexShader(vertexShaderDefault);
        materialTrees.setFragmentShader(fragShaderDefault);
        Texture textureTree = new FileTexture("textures/trees.jpg");
        materialTrees.setTexture("diffuseMap", textureTree);
        materialTrees.getParameters().setVector4f("color", new Vector4f(0, 0, 1, 1));

        Geometry geometryTrees = new Geometry();
        geometryTrees.setMesh(meshTrees);
        geometryTrees.setMaterial(materialTrees);
        geometryTrees.move(new Vector3f(0, -1, 0));
        geometryTrees.scale(new Vector3f(0.01f, 0.01f, 0.01f));
        rootNode.add(geometryTrees);

        // Dennis

        Mesh meshDennis = new Mesh();
        meshDennis.loadModel("models/dennis.obj");

        Material materialDennis = new Material();
        materialDennis.setVertexShader(vertexShaderDefault);
        materialDennis.setFragmentShader(fragShaderDefault);
        Texture textureDennis = new FileTexture("textures/dennis.jpg");
        textureDennis.init(this);
        materialDennis.setTexture("diffuseMap", textureDennis);
        materialDennis.getParameters().setVector4f("color", new Vector4f(1, 1, 0, 1));

        geometryDennis = new Geometry();
        geometryDennis.setMesh(meshDennis);
        geometryDennis.setMaterial(materialDennis);
        geometryDennis.move(new Vector3f(0, -1, 0));
        geometryDennis.scale(new Vector3f(0.005f, 0.005f, 0.005f));

        // Duck

        Node nodeDuck = new GltfModelLoader("models/duck.gltf").load();
        nodeDuck.setLocalRotation(new Quaternionf(new AxisAngle4f((float) Math.toRadians(-90), 1, 0, 0)));

        Node nodeDuckWrapper = new Node();
        nodeDuckWrapper.add(nodeDuck);
        nodeDuckWrapper.move(new Vector3f(1, -1.5f, -0.5f));
        nodeDuckWrapper.scale(new Vector3f(0.25f, 0.25f, 0.25f));
        rootNode.add(nodeDuckWrapper);

        // Knot

        Mesh meshKnot = new Mesh();
        meshKnot.loadModel("models/knot.obj");

        Material materialKnot = new Material();
        materialKnot.setVertexShader(vertexShaderDefault);
        materialKnot.setFragmentShader(fragShaderDefault);
        Texture textureKnot = new FileTexture("textures/chalet.jpg");
        materialKnot.setTexture("diffuseMap", textureKnot);
        materialKnot.getParameters().setVector4f("color", new Vector4f(0, 1, 0, 1));

        Geometry geometryKnot = new Geometry();
        geometryKnot.setMesh(meshKnot);
        geometryKnot.setMaterial(materialKnot);
        geometryKnot.move(new Vector3f(-1.5f, -0.2f, 0.5f));
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
            if (spatial != geometryGround) {
                spatial.setLocalRotation(new Quaternionf(new AxisAngle4f((float) (time * Math.toRadians(90)), 0.0f, 0.0f, 1.0f)));
            }
        }
        materialCool.getParameters().setFloat("time", time);
        camera.setLocation(camera.getLocation().add(cameraMoveDirection.mul(tpf * 3, new Vector3f())));
    }
}
