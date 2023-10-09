package com.destrostudios.icetea.core.render.shadow;

import com.destrostudios.icetea.core.render.EssentialGeometryRenderPipelineState;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
public class ShadowMapRenderPipelineStateEssential extends EssentialGeometryRenderPipelineState {

    public ShadowMapRenderPipelineStateEssential(String jobId) {
        super(jobId);
    }
}
