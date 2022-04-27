package com.destrostudios.icetea.core.resource;

import com.destrostudios.icetea.core.scene.Geometry;

import java.util.Map;

public interface AdditionalResourceDescriptorProvider {

    void addAdditionalResourceDescriptors(Geometry geometry, Map<String, ResourceDescriptor<?>> resourceDescriptors);
}
