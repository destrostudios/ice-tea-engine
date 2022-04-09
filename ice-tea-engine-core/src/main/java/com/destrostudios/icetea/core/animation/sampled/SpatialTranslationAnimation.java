package com.destrostudios.icetea.core.animation.sampled;

import com.destrostudios.icetea.core.animation.AnimationSamplerData;
import com.destrostudios.icetea.core.animation.SampledAnimation;
import com.destrostudios.icetea.core.animation.samplers.Vector3fAnimationSampler;
import com.destrostudios.icetea.core.clone.CloneContext;
import com.destrostudios.icetea.core.scene.Spatial;
import org.joml.Vector3f;

public class SpatialTranslationAnimation extends SampledAnimation<Vector3f> {

    public SpatialTranslationAnimation(AnimationSamplerData<Vector3f> samplerData, Spatial spatial) {
        super(new Vector3fAnimationSampler(samplerData));
        this.spatial = spatial;
    }

    public SpatialTranslationAnimation(SpatialTranslationAnimation spatialTranslationAnimation, CloneContext context) {
        super(spatialTranslationAnimation);
        spatial = context.cloneByReference(spatialTranslationAnimation.spatial);
    }
    private Spatial spatial;

    @Override
    protected void setValue(Vector3f value) {
        spatial.setLocalTranslation(value);
    }

    @Override
    public SpatialTranslationAnimation clone(CloneContext context) {
        return new SpatialTranslationAnimation(this, context);
    }
}
