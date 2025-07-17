package com.destrostudios.icetea.core.render;

import com.destrostudios.icetea.core.Application;
import com.destrostudios.icetea.core.data.VertexData;
import com.destrostudios.icetea.core.data.values.DataValue;
import com.destrostudios.icetea.core.mesh.Mesh;

import java.util.stream.Collectors;

public abstract class MeshRenderPipelineCreator<RJ extends RenderJob<?>, PS extends MeshRenderPipelineState> extends RenderPipelineCreator<RJ, PS> {

    public MeshRenderPipelineCreator(Application application, RJ renderJob) {
        super(application, renderJob);
    }

    public void fillMeshState(PS state, Mesh mesh) {
        VertexData referenceVertex = mesh.getVertices()[0];
        state.setVertexSize(referenceVertex.getSize());
        state.setVertexFields(referenceVertex.getFields().entrySet().stream()
            .map((entry) -> {
                String name = entry.getKey();
                DataValue<?> dataValue = entry.getValue();
                return new MeshRenderPipelineState.VertexField(
                    name,
                    dataValue.getShaderDefinitionType(),
                    dataValue.getFormat(),
                    dataValue.getSize()
                );
            })
            .collect(Collectors.toList()));
        state.setTopology(mesh.getTopology());
    }
}
