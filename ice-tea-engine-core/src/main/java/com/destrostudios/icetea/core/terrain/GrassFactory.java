package com.destrostudios.icetea.core.terrain;

import com.destrostudios.icetea.core.asset.AssetManager;
import com.destrostudios.icetea.core.material.Material;
import com.destrostudios.icetea.core.mesh.Grid;
import com.destrostudios.icetea.core.scene.Geometry;
import com.destrostudios.icetea.core.shader.Shader;
import org.joml.Vector4f;

import static org.lwjgl.vulkan.VK10.VK_CULL_MODE_NONE;

public class GrassFactory {

    public static Geometry createGrass(int patches, AssetManager assetManager) {
        Geometry geometry = new Geometry();
        geometry.setMesh(new Grid(patches));
        Material material = new Material();
        material.setVertexShader(new Shader("shaders/grass/grass.vert"));
        material.setFragmentShader(new Shader("shaders/grass/grass.frag", new String[] { "light", "shadow" }));
        material.setTesselationPatchSize(16);
        material.setTesselationControlShader(new Shader("shaders/grass/grass.tesc"));
        material.setTesselationEvaluationShader(new Shader("shaders/grass/grass.tese"));
        material.setGeometryShader(new Shader("shaders/grass/grass.geom", new String[] { "noise", "light", "shadow" }));
        material.getParameters().setVector4f("baseColor", new Vector4f(0.21244211f, 0.5849056f, 0.22120592f, 1));
        material.getParameters().setVector4f("tipColor", new Vector4f(0.47770557f, 0.8962264f, 0.48615223f, 1));
        material.getParameters().setFloat("tesselationFactor", 20f);
        material.getParameters().setFloat("tesselationSlope", 1f);
        material.getParameters().setFloat("tesselationShift", 0f);
        material.getParameters().setFloat("time", 0f);
        material.setTexture("windMap", assetManager.loadTexture("textures/grass/wind.png"));
        material.setTexture("bladeMap", assetManager.loadTexture("textures/grass/blade.png"));
        material.setCullMode(VK_CULL_MODE_NONE);
        geometry.setMaterial(material);
        return geometry;
    }
}
