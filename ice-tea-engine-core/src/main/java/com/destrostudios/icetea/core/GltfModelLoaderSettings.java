package com.destrostudios.icetea.core;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class GltfModelLoaderSettings {
    @Builder.Default
    private boolean generateNormals;
}
