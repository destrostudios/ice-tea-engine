package com.destrostudios.icetea.core.water;

import com.destrostudios.icetea.core.Geometry;
import com.destrostudios.icetea.core.meshes.Grid;

public class WaterFactory {

    public static Geometry createWater(WaterConfig waterConfig) {
        Geometry geometry = new Geometry();
        geometry.setMesh(new Grid(waterConfig.getPatches()));
        geometry.addControl(new WaterControl(waterConfig));
        return geometry;
    }
}