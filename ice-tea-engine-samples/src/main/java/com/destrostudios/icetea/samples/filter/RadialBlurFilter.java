package com.destrostudios.icetea.samples.filter;

import com.destrostudios.icetea.core.filter.Filter;
import com.destrostudios.icetea.core.shader.FileShader;

public class RadialBlurFilter extends Filter {

    public RadialBlurFilter() {
        this(1, 2.2f);
    }

    public RadialBlurFilter(float sampleDist, float sampleStrength) {
        this.sampleDist = sampleDist;
        this.sampleStrength = sampleStrength;
        fragmentShader = new FileShader("com/destrostudios/icetea/samples/shaders/filters/radialBlur.frag");
    }
    private float sampleDist;
    private float sampleStrength;

    @Override
    protected void updateUniformBuffer() {
        super.updateUniformBuffer();
        uniformBuffer.getData().setFloat("sampleDist", sampleDist);
        uniformBuffer.getData().setFloat("sampleStrength", sampleStrength);
    }
}
