package com.destrostudios.icetea.core.animation.sampled;

import com.destrostudios.icetea.core.animation.AnimationSamplerData;
import com.destrostudios.icetea.core.animation.SampledAnimation;
import com.destrostudios.icetea.core.animation.samplers.QuaternionAnimationSampler;
import com.destrostudios.icetea.core.clone.CloneContext;
import com.destrostudios.icetea.core.scene.Spatial;
import org.joml.Quaternionf;

public class SpatialRotationAnimation extends SampledAnimation<Quaternionf> {

    public SpatialRotationAnimation(AnimationSamplerData<Quaternionf> samplerData, Spatial spatial) {
        super(new QuaternionAnimationSampler(samplerData));
        this.spatial = spatial;
    }

    public SpatialRotationAnimation(SpatialRotationAnimation spatialRotationAnimation, CloneContext context) {
        super(spatialRotationAnimation);
        spatial = context.cloneByReference(spatialRotationAnimation.spatial);
    }
    private Spatial spatial;

    @Override
    protected void setValue(Quaternionf value) {
        spatial.setLocalRotation(value);
    }

    @Override
    public SpatialRotationAnimation clone(CloneContext context) {
        return new SpatialRotationAnimation(this, context);
    }
}
