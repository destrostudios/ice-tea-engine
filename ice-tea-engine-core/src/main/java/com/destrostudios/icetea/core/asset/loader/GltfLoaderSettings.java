package com.destrostudios.icetea.core.asset.loader;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Setter
@Getter
public class GltfLoaderSettings {
    @Builder.Default
    private boolean generateNormals;
}
