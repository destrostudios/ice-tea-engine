package com.destrostudios.icetea.core.material.descriptor;

import com.destrostudios.icetea.core.scene.Geometry;

import java.util.List;

public interface AdditionalMaterialDescriptorProvider {

    List<MaterialDescriptorWithLayout> getAdditionalMaterialDescriptors(Geometry geometry);
}
