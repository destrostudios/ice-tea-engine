package com.destrostudios.icetea.core.animation.sampled;

import com.destrostudios.icetea.core.animation.AnimationSamplerData;
import com.destrostudios.icetea.core.animation.SampledAnimation;
import com.destrostudios.icetea.core.animation.samplers.QuaternionAnimationSampler;
import com.destrostudios.icetea.core.clone.CloneContext;
import com.destrostudios.icetea.core.model.Joint;
import org.joml.Quaternionf;

public class JointRotationAnimation extends SampledAnimation<Quaternionf> {

    public JointRotationAnimation(AnimationSamplerData<Quaternionf> samplerData, Joint joint) {
        super(new QuaternionAnimationSampler(samplerData));
        this.joint = joint;
    }

    public JointRotationAnimation(JointRotationAnimation jointRotationAnimation, CloneContext context) {
        super(jointRotationAnimation);
        joint = context.cloneByReference(jointRotationAnimation.joint);
    }
    private Joint joint;

    @Override
    protected void setValue(Quaternionf value) {
        joint.setLocalPoseRotation(value);
    }

    @Override
    public JointRotationAnimation clone(CloneContext context) {
        return new JointRotationAnimation(this, context);
    }
}
