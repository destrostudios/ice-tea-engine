package com.destrostudios.icetea.samples.terrain;

import com.destrostudios.icetea.core.material.Material;
import com.destrostudios.icetea.core.mesh.Grid;
import com.destrostudios.icetea.core.scene.Geometry;
import com.destrostudios.icetea.core.shader.Shader;

public class TerrainFactory {

    public static Geometry createTerrain(int patches) {
        Geometry geometry = new Geometry();
        geometry.setMesh(new Grid(patches));
        Material material = new Material();
        material.setVertexShader(new Shader("com/destrostudios/icetea/samples/shaders/terrain/terrain.vert"));
        material.setFragmentShader(new Shader("com/destrostudios/icetea/samples/shaders/terrain/terrain.frag", new String[] {
            "com/destrostudios/icetea/core/shaders/nodes/light.glsllib",
            "com/destrostudios/icetea/core/shaders/nodes/shadow.glsllib"
        }));
        material.setTessellationPatchSize(16);
        material.setTessellationControlShader(new Shader("com/destrostudios/icetea/samples/shaders/terrain/terrain.tesc"));
        material.setTessellationEvaluationShader(new Shader("com/destrostudios/icetea/samples/shaders/terrain/terrain.tese", new String[] {
            "com/destrostudios/icetea/samples/shaders/nodes/noise.glsllib",
        }));
        material.setGeometryShader(new Shader("com/destrostudios/icetea/samples/shaders/terrain/terrain.geom", new String[] {
            "com/destrostudios/icetea/core/shaders/nodes/light.glsllib",
            "com/destrostudios/icetea/core/shaders/nodes/shadow.glsllib"
        }));
        material.getParameters().setFloat("tessellationFactor", 0.1f);
        material.getParameters().setFloat("tessellationSlope", 0.5f);
        material.getParameters().setFloat("tessellationShift", 0.1f);
        geometry.setMaterial(material);
        return geometry;
    }
}
