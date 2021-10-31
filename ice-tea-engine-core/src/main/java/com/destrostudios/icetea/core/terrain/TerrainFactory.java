package com.destrostudios.icetea.core.terrain;

import com.destrostudios.icetea.core.material.Material;
import com.destrostudios.icetea.core.mesh.Grid;
import com.destrostudios.icetea.core.scene.Geometry;
import com.destrostudios.icetea.core.shader.Shader;

public class TerrainFactory {

    public static Geometry createTerrain(int patches) {
        Geometry geometry = new Geometry();
        geometry.setMesh(new Grid(patches));
        Material material = new Material();
        material.setVertexShader(new Shader("shaders/terrain/terrain.vert"));
        material.setFragmentShader(new Shader("shaders/terrain/terrain.frag", new String[] { "light", "shadow" }));
        material.setTesselationPatchSize(16);
        material.setTesselationControlShader(new Shader("shaders/terrain/terrain.tesc"));
        material.setTesselationEvaluationShader(new Shader("shaders/terrain/terrain.tese", new String[] { "noise" }));
        material.setGeometryShader(new Shader("shaders/terrain/terrain.geom", new String[] { "light", "shadow" }));
        material.getParameters().setFloat("tessellationFactor", 0.1f);
        material.getParameters().setFloat("tessellationSlope", 0.5f);
        material.getParameters().setFloat("tessellationShift", 0.1f);
        geometry.setMaterial(material);
        return geometry;
    }
}
