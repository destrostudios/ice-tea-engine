package com.destrostudios.icetea.samples.filter;

import com.destrostudios.icetea.core.filter.Filter;
import com.destrostudios.icetea.core.shader.Shader;

public class RadialBlurFilter extends Filter {

    public RadialBlurFilter() {
        this(1, 2.2f);
    }

    public RadialBlurFilter(float sampleDist, float sampleStrength) {
        this.sampleDist = sampleDist;
        this.sampleStrength = sampleStrength;
        fragmentShader = new Shader("com/destrostudios/icetea/samples/shaders/filters/radialBlur.frag");
    }
    private float sampleDist;
    private float sampleStrength;

    @Override
    protected void updateUniformData() {
        super.updateUniformData();
        uniformData.setFloat("sampleDist", sampleDist);
        uniformData.setFloat("sampleStrength", sampleStrength);
    }
}
