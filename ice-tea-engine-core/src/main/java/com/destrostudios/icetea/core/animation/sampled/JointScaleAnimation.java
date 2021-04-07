package com.destrostudios.icetea.core.animation.sampled;

import com.destrostudios.icetea.core.animation.AnimationSamplerData;
import com.destrostudios.icetea.core.animation.SampledAnimation;
import com.destrostudios.icetea.core.animation.samplers.Vector3fAnimationSampler;
import com.destrostudios.icetea.core.model.Joint;
import org.joml.Vector3f;

public class JointScaleAnimation extends SampledAnimation<Vector3f> {

    public JointScaleAnimation(AnimationSamplerData<Vector3f> samplerData, Joint joint) {
        super(new Vector3fAnimationSampler(samplerData));
        this.joint = joint;
    }
    private Joint joint;

    @Override
    protected void setValue(Vector3f value) {
        joint.setLocalPoseScale(value);
    }
}
