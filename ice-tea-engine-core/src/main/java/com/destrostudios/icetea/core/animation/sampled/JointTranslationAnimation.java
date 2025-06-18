package com.destrostudios.icetea.core.animation.sampled;

import com.destrostudios.icetea.core.animation.AnimationSamplerData;
import com.destrostudios.icetea.core.animation.SampledAnimation;
import com.destrostudios.icetea.core.animation.samplers.Vector3fAnimationSampler;
import com.destrostudios.icetea.core.clone.CloneContext;
import com.destrostudios.icetea.core.model.Joint;
import org.joml.Vector3f;

public class JointTranslationAnimation extends SampledAnimation<Vector3f> {

    public JointTranslationAnimation(AnimationSamplerData<Vector3f> samplerData, Joint joint) {
        super(new Vector3fAnimationSampler(samplerData));
        this.joint = joint;
    }

    public JointTranslationAnimation(JointTranslationAnimation jointTranslationAnimation, CloneContext context) {
        super(jointTranslationAnimation);
        joint = context.cloneByReference(jointTranslationAnimation.joint);
    }
    private Joint joint;

    @Override
    protected void setValue(Vector3f value) {
        joint.setLocalTranslation(value);
    }

    @Override
    public JointTranslationAnimation clone(CloneContext context) {
        return new JointTranslationAnimation(this, context);
    }
}
