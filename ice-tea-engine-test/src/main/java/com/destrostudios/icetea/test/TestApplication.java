package com.destrostudios.icetea.test;

import com.destrostudios.icetea.core.Application;
import com.destrostudios.icetea.core.Geometry;
import com.destrostudios.icetea.core.Material;
import com.destrostudios.icetea.core.Texture;
import org.joml.AxisAngle4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import static org.lwjgl.glfw.GLFW.glfwGetTime;

public class TestApplication extends Application {

    @Override
    protected void initScene() {
        Material material = new Material();
        material.setVertexShaderFile("shaders/my_shader.vert");
        material.setFragmentShaderFile("shaders/my_shader.frag");
        Texture texture = new Texture(this, "textures/chalet.jpg");
        material.addTexture(texture);

        Geometry geometry1 = new Geometry();
        geometry1.loadModel("models/chalet.obj");
        geometry1.setMaterial(material);
        geometry1.init(this);
        geometry1.rotate(new Quaternionf(new AxisAngle4f((float) Math.toRadians(45), 0, 0, 1)));
        geometry1.move(new Vector3f(1.5f, 0, 0));
        geometry1.scale(new Vector3f(0.5f, 0.5f, 1));
        geometries.add(geometry1);

        Geometry geometry2 = new Geometry();
        geometry2.loadModel("models/chalet.obj");
        geometry2.setMaterial(material);
        geometry2.init(this);
        geometries.add(geometry2);
    }

    @Override
    protected void update() {
        for (Geometry geometry : geometries) {
            geometry.setLocalRotation(new Quaternionf(new AxisAngle4f((float) (glfwGetTime() * Math.toRadians(90)), 0.0f, 0.0f, 1.0f)));
        }
    }
}
