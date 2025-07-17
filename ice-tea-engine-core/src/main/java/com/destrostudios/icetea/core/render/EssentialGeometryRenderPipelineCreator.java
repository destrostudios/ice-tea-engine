package com.destrostudios.icetea.core.render;

import com.destrostudios.icetea.core.Application;
import com.destrostudios.icetea.core.material.Material;
import com.destrostudios.icetea.core.mesh.Mesh;
import com.destrostudios.icetea.core.resource.ResourceDescriptorSet;
import com.destrostudios.icetea.core.scene.Geometry;

public abstract class EssentialGeometryRenderPipelineCreator<RJ extends RenderJob<?>, PS extends EssentialGeometryRenderPipelineState> extends MeshRenderPipelineCreator<RJ, PS> {

    public EssentialGeometryRenderPipelineCreator(Application application, RJ renderJob) {
        super(application, renderJob);
    }

    protected void fillEssentialGeometryState(PS state, GeometryRenderContext<RJ> geometryRenderContext) {
        Geometry geometry = geometryRenderContext.getGeometry();
        Mesh mesh = geometry.getMesh();
        Material material = geometry.getMaterial();
        ResourceDescriptorSet resourceDescriptorSet = geometryRenderContext.getResourceDescriptorSet();

        // Mesh
        fillMeshState(state, mesh);

        // Material
        state.setVertexShader(material.getVertexShader());
        state.setTessellationControlShader(material.getTessellationControlShader());
        state.setTessellationEvaluationShader(material.getTessellationEvaluationShader());
        state.setGeometryShader(material.getGeometryShader());
        state.setTessellationPatchSize(material.getTessellationPatchSize());
        state.setPolygonMode(material.getFillMode());
        state.setCullMode(material.getCullMode());
        state.setDepthTest(material.isDepthTest());
        state.setDepthWrite(material.isDepthWrite());
        state.setTransparent(material.isTransparent());

        // GeometryRenderer
        state.setDynamicStates(geometry.getRenderer().getDynamicStates());

        // ResourceDescriptorSet
        state.setDescriptorSetShaderDeclaration(resourceDescriptorSet.getShaderDeclaration());
    }
}
