package com.destrostudios.icetea.core.render.cubemap;

import com.destrostudios.icetea.core.render.MeshRenderPipelineState;
import com.destrostudios.icetea.core.shader.Shader;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
public class CubeMapRenderPipelineState extends MeshRenderPipelineState {

    public CubeMapRenderPipelineState(String jobId) {
        super(jobId);
    }
    private Shader fragmentShader;
    private String descriptorSetShaderDeclaration;
}
