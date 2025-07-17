package com.destrostudios.icetea.core.render;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
public class MeshRenderPipelineState extends PipelineState {

    public MeshRenderPipelineState(String jobId) {
        super(jobId);
    }
    private int vertexSize;
    private List<VertexField> vertexFields;
    private int topology;

    @AllArgsConstructor
    @Getter
    @EqualsAndHashCode
    public static class VertexField {
        private String name;
        private String shaderDefinitionType;
        private int format;
        private int size;
    }
}
