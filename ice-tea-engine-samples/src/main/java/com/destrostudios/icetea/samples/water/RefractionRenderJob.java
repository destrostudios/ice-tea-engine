package com.destrostudios.icetea.samples.water;

import com.destrostudios.icetea.core.scene.Geometry;
import com.destrostudios.icetea.core.render.scene.SceneRenderJob;

public class RefractionRenderJob extends SceneRenderJob {

    public RefractionRenderJob(Geometry geometryWater) {
        this.geometryWater = geometryWater;
    }
    private Geometry geometryWater;

    @Override
    public boolean isRendering(Geometry geometry) {
        return ((geometry != geometryWater) && geometry.hasParent(application.getSceneNode()));
    }
}
