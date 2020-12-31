package com.destrostudios.icetea.core.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class GltfModelLoaderSettings {
    @Builder.Default
    private boolean generateNormals;
}
