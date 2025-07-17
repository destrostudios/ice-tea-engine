package com.destrostudios.icetea.test;

import com.destrostudios.icetea.core.Application;
import com.destrostudios.icetea.core.asset.loader.GltfLoaderSettings;
import com.destrostudios.icetea.core.asset.locator.FileLocator;
import com.destrostudios.icetea.core.camera.systems.CameraKeyMoveSystem;
import com.destrostudios.icetea.core.camera.systems.CameraMouseRotateSystem;
import com.destrostudios.icetea.core.clone.CloneContext;
import com.destrostudios.icetea.core.pbr.PbrConfig;
import com.destrostudios.icetea.core.light.DirectionalLight;
import com.destrostudios.icetea.core.light.PointLight;
import com.destrostudios.icetea.core.material.Material;
import com.destrostudios.icetea.core.scene.Node;
import com.destrostudios.icetea.core.scene.background.BackgroundFactory;
import org.joml.AxisAngle4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public class TestPBR extends Application {

    public static void main(String[] args) {
        new TestPBR().start();
    }

    public TestPBR() {
        config.setEnableValidationLayer(true);
        config.setDisplayFpsInTitle(true);
    }

    @Override
    protected void init() {
        super.init();
        assetManager.addLocator(new FileLocator("./assets"));

        sceneCamera.setLocation(new Vector3f(0, 0, 20));

        DirectionalLight directionalLight = new DirectionalLight();
        directionalLight.setDirection(new Vector3f(0, 0, -1).normalize());
        setLight(directionalLight);

        PointLight pointLight = new PointLight();
        pointLight.setPosition(new Vector3f(0, 10, 10));
        // setLight(pointLight);

        sceneNode.setAffectedByLight(true);

        setPbrEnvironment(pbrManager.createEnvironmentByHdrMap("com/destrostudios/icetea/samples/textures/pbr/papermill.hdr", new PbrConfig()));
        sceneNode.add(BackgroundFactory.createCubeMapBackground(getPbrEnvironment().getEnvironmentMap()));

        int rows = 7;
        int columns = 7;
        float spacing = 2.5f;
        for (int row = 0; row < rows; row++) {
            float metallic = ((float) row) / rows;
            for (int col = 0; col < columns; col++) {
                float roughness = ((float) col) / columns;

                Node duck = (Node) assetManager.loadModel("models/duck/Duck.gltf");
                duck.forEachGeometry(geometry -> {
                    Material material = geometry.getMaterial().clone(CloneContext.reuseAll());
                    material.getParameters().setFloat("metallic", metallic);
                    material.getParameters().setFloat("roughness", roughness);
                    geometry.setMaterial(material);
                });
                duck.move(new Vector3f((col - (columns / 2f)) * spacing, (row - (rows / 2f)) * spacing, -2));
                duck.scale(new Vector3f(1.5f));
                sceneNode.add(duck);
            }
        }

        Node footman = (Node) assetManager.loadModel("models/footman/scene.gltf", GltfLoaderSettings.builder().bakeGeometries(true).build());
        footman.move(new Vector3f(0, -5, 0));
        footman.rotate(new Quaternionf(new AxisAngle4f((float) Math.PI, 1, 0, 0)));
        footman.scale(new Vector3f(7));
        sceneNode.add(footman);

        addSystem(new CameraMouseRotateSystem(sceneCamera));
        addSystem(new CameraKeyMoveSystem(sceneCamera));
    }
}
