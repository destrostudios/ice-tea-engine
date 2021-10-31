package com.destrostudios.icetea.core.terrain;

import com.destrostudios.icetea.core.asset.AssetManager;
import com.destrostudios.icetea.core.material.Material;
import com.destrostudios.icetea.core.mesh.Grid;
import com.destrostudios.icetea.core.scene.Geometry;
import com.destrostudios.icetea.core.shader.Shader;

import static org.lwjgl.vulkan.VK10.VK_CULL_MODE_NONE;

public class GrassFactory {

    public static Geometry createGrass(GrassConfig grassConfig, AssetManager assetManager) {
        Geometry geometry = new Geometry();
        geometry.setMesh(new Grid(grassConfig.getPatches()));
        geometry.setMaterial(createMaterial(grassConfig, assetManager));
        return geometry;
    }

    private static Material createMaterial(GrassConfig grassConfig, AssetManager assetManager) {
        Material material = new Material();
        material.setVertexShader(new Shader("shaders/grass/grass.vert"));
        material.setFragmentShader(new Shader("shaders/grass/grass.frag", new String[] { "light", "shadow" }));
        material.setTesselationPatchSize(16);
        material.setTesselationControlShader(new Shader("shaders/grass/grass.tesc"));
        material.setTesselationEvaluationShader(new Shader("shaders/grass/grass.tese"));
        material.setGeometryShader(new Shader("shaders/grass/grass.geom", new String[] { "light", "shadow" }));
        // FIXME: Vectors have to be defined first in this order or somehow the memory alignment is messed up
        material.getParameters().setVector4f("baseColor", grassConfig.getBaseColor());
        material.getParameters().setVector4f("tipColor", grassConfig.getTipColor());
        material.getParameters().setVector2f("windVelocity", grassConfig.getWindVelocity());
        material.getParameters().setFloat("tessellationFactor", grassConfig.getTessellationFactor());
        material.getParameters().setFloat("tessellationSlope", grassConfig.getTessellationSlope());
        material.getParameters().setFloat("tessellationShift", grassConfig.getTessellationShift());
        material.getParameters().setFloat("bladeWidth", grassConfig.getBladeWidth());
        material.getParameters().setFloat("bladeHeight", grassConfig.getBladeHeight());
        material.getParameters().setFloat("bladeForward", grassConfig.getBladeForward());
        material.getParameters().setFloat("bladeBend", grassConfig.getBladeBend());
        material.getParameters().setInt("bladeSegments", grassConfig.getBladeSegments());
        material.getParameters().setFloat("windFrequency", grassConfig.getWindFrequency());
        material.getParameters().setFloat("windFrequency", grassConfig.getWindFrequency());
        material.setTexture("windMap", assetManager.loadTexture(grassConfig.getWindMapFilePath()));
        material.getParameters().setFloat("windMaxAngle", grassConfig.getWindMaxAngle());
        material.setTexture("bladeMap", assetManager.loadTexture(grassConfig.getBladeMapFilePath()));
        material.getParameters().setFloat("time", 0f);
        material.setCullMode(VK_CULL_MODE_NONE);
        return material;
    }
}
