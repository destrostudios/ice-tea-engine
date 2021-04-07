package com.destrostudios.icetea.core.animation.sampled;

import com.destrostudios.icetea.core.animation.AnimationSamplerData;
import com.destrostudios.icetea.core.animation.SampledAnimation;
import com.destrostudios.icetea.core.animation.samplers.Vector3fAnimationSampler;
import com.destrostudios.icetea.core.scene.Spatial;
import org.joml.Vector3f;

public class SpatialScaleAnimation extends SampledAnimation<Vector3f> {

    public SpatialScaleAnimation(AnimationSamplerData<Vector3f> samplerData, Spatial spatial) {
        super(new Vector3fAnimationSampler(samplerData));
        this.spatial = spatial;
    }
    private Spatial spatial;

    @Override
    protected void setValue(Vector3f value) {
        spatial.setLocalScale(value);
    }
}
