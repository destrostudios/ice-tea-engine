package com.destrostudios.icetea.core.render.fullscreen;

import com.destrostudios.icetea.core.render.PipelineState;
import com.destrostudios.icetea.core.shader.Shader;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
public class FullScreenQuadRenderPipelineState extends PipelineState {

    public FullScreenQuadRenderPipelineState(String jobId) {
        super(jobId);
    }
    private Shader fragmentShader;
}
