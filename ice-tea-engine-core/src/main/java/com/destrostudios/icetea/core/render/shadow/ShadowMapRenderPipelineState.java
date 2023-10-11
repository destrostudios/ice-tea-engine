package com.destrostudios.icetea.core.render.shadow;

import com.destrostudios.icetea.core.render.EssentialGeometryRenderPipelineState;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
public class ShadowMapRenderPipelineState extends EssentialGeometryRenderPipelineState {

    public ShadowMapRenderPipelineState(String jobId) {
        super(jobId);
    }
}
