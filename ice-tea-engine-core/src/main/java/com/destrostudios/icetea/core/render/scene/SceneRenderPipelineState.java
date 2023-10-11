package com.destrostudios.icetea.core.render.scene;

import com.destrostudios.icetea.core.render.EssentialGeometryRenderPipelineState;
import com.destrostudios.icetea.core.shader.Shader;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
public class SceneRenderPipelineState extends EssentialGeometryRenderPipelineState {

    public SceneRenderPipelineState(String jobId) {
        super(jobId);
    }
    // Material
    private Shader fragmentShader;
}
