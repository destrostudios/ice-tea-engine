package com.destrostudios.icetea.core.asset.loader;

import com.destrostudios.icetea.core.clone.CloneContext;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Setter
@Getter
public class GltfLoaderSettings {
    private boolean bakeGeometries;
    @Builder.Default
    private CloneContext cloneContext = CloneContext.reuseAll();
}
