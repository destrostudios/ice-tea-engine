package com.destrostudios.icetea.core.water;

import com.destrostudios.icetea.core.Geometry;
import com.destrostudios.icetea.core.SceneRenderJob;

public class RefractionRenderJob extends SceneRenderJob {

    public RefractionRenderJob(Geometry geometryWater) {
        this.geometryWater = geometryWater;
    }
    private Geometry geometryWater;

    @Override
    public boolean isRendering(Geometry geometry) {
        return (geometry != geometryWater);
    }
}
