package com.destrostudios.icetea.core.render;

import com.destrostudios.icetea.core.shader.Shader;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
public class EssentialGeometryRenderPipelineState extends PipelineState {

    public EssentialGeometryRenderPipelineState(String jobId) {
        super(jobId);
    }
    // Mesh
    private int vertexSize;
    private List<VertexField> vertexFields;
    private int topology;

    // Material
    private Shader vertexShader;
    private Shader tessellationControlShader;
    private Shader tessellationEvaluationShader;
    private Shader geometryShader;
    private int tessellationPatchSize;
    private int polygonMode;
    private int cullMode;
    private boolean depthTest;
    private boolean depthWrite;
    private boolean transparent;

    // GeometryRenderer
    private int[] dynamicStates;

    // ResourceDescriptorSet
    private String descriptorSetShaderDeclaration;

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
