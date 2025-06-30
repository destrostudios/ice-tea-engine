package com.destrostudios.icetea.samples.terrain;

import com.destrostudios.icetea.core.material.Material;
import com.destrostudios.icetea.core.mesh.Grid;
import com.destrostudios.icetea.core.scene.Geometry;
import com.destrostudios.icetea.core.shader.FileShader;

public class TerrainFactory {

    public static Geometry createTerrain(int patches) {
        Geometry geometry = new Geometry();
        geometry.setMesh(new Grid(patches));
        Material material = new Material();
        material.setVertexShader(new FileShader("com/destrostudios/icetea/samples/shaders/terrain/terrain.vert"));
        material.setFragmentShader(new FileShader("com/destrostudios/icetea/samples/shaders/terrain/terrain.frag"));
        material.setTessellationPatchSize(16);
        material.setTessellationControlShader(new FileShader("com/destrostudios/icetea/samples/shaders/terrain/terrain.tesc"));
        material.setTessellationEvaluationShader(new FileShader("com/destrostudios/icetea/samples/shaders/terrain/terrain.tese"));
        material.setGeometryShader(new FileShader("com/destrostudios/icetea/samples/shaders/terrain/terrain.geom"));
        material.getParameters().setFloat("tessellationFactor", 0.1f);
        material.getParameters().setFloat("tessellationSlope", 0.5f);
        material.getParameters().setFloat("tessellationShift", 0.1f);
        geometry.setMaterial(material);
        return geometry;
    }
}
