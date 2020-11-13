package com.destrostudios.icetea.test;

import com.destrostudios.icetea.core.*;
import org.joml.AxisAngle4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import static org.lwjgl.glfw.GLFW.glfwGetTime;

public class TestApplication extends Application {

    private Geometry geometryDennis;
    private boolean hasAddedDennis;
    private boolean hasRemovedDennis;

    @Override
    protected void initScene() {
        camera.setLocation(new Vector3f(0, -3, 1.25f));
        camera.setDirection(new Vector3f(0, 1, -0.25f).normalize());

        // Chalet

        Mesh meshChalet = new Mesh();
        meshChalet.loadModel("models/chalet.obj");

        Material materialChalet = new Material();
        materialChalet.setVertexShaderFile("shaders/my_shader.vert");
        materialChalet.setFragmentShaderFile("shaders/my_shader.frag");
        Texture textureChalet = new Texture(this, "textures/chalet.jpg");
        materialChalet.addTexture(textureChalet);

        Geometry geometryChalet1 = new Geometry();
        geometryChalet1.setMesh(meshChalet);
        geometryChalet1.setMaterial(materialChalet);
        sceneGraph.addGeometry(geometryChalet1);

        Geometry geometryChalet2 = new Geometry();
        geometryChalet2.setMesh(meshChalet);
        geometryChalet2.setMaterial(materialChalet);
        sceneGraph.addGeometry(geometryChalet2);
        geometryChalet2.move(new Vector3f(1.5f, 1, 0));
        geometryChalet2.rotate(new Quaternionf(new AxisAngle4f((float) Math.toRadians(45), 0, 0, 1)));
        geometryChalet2.scale(new Vector3f(0.5f, 0.5f, 1));

        // Trees

        Mesh meshTrees = new Mesh();
        meshTrees.loadModel("models/trees.obj");

        Material materialTrees = new Material();
        materialTrees.setVertexShaderFile("shaders/my_shader.vert");
        materialTrees.setFragmentShaderFile("shaders/my_shader.frag");
        Texture textureTree = new Texture(this, "textures/trees.jpg");
        materialTrees.addTexture(textureTree);

        Geometry geometryTrees = new Geometry();
        geometryTrees.setMesh(meshTrees);
        geometryTrees.setMaterial(materialTrees);
        sceneGraph.addGeometry(geometryTrees);
        geometryTrees.move(new Vector3f(0, -1, 0));
        geometryTrees.scale(new Vector3f(0.01f, 0.01f, 0.01f));

        // Dennis

        Mesh meshDennis = new Mesh();
        meshDennis.loadModel("models/dennis.obj");

        Material materialDennis = new Material();
        materialDennis.setVertexShaderFile("shaders/my_shader.vert");
        materialDennis.setFragmentShaderFile("shaders/my_shader.frag");
        Texture textureDennis = new Texture(this, "textures/dennis.jpg");
        materialDennis.addTexture(textureDennis);

        geometryDennis = new Geometry();
        geometryDennis.setMesh(meshDennis);
        geometryDennis.setMaterial(materialDennis);
        geometryDennis.move(new Vector3f(0, -1, 0));
        geometryDennis.scale(new Vector3f(0.005f, 0.005f, 0.005f));
    }

    @Override
    protected void update() {
        double time = glfwGetTime();
        if ((time > 6) && (!hasAddedDennis)) {
            sceneGraph.addGeometry(geometryDennis);
            hasAddedDennis = true;
        } else if ((time > 10) && (!hasRemovedDennis)) {
            sceneGraph.removeGeometry(geometryDennis);
            hasRemovedDennis = true;
        }
        for (Geometry geometry : sceneGraph.getGeometries()) {
            geometry.setLocalRotation(new Quaternionf(new AxisAngle4f((float) (time * Math.toRadians(90)), 0.0f, 0.0f, 1.0f)));
        }
    }
}
