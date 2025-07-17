package com.destrostudios.icetea.core.render;

import com.destrostudios.icetea.core.shader.Shader;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
public class EssentialGeometryRenderPipelineState extends MeshRenderPipelineState {

    public EssentialGeometryRenderPipelineState(String jobId) {
        super(jobId);
    }
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
}
