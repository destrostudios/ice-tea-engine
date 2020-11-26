package com.destrostudios.icetea.core.filters;

import com.destrostudios.icetea.core.Application;
import com.destrostudios.icetea.core.Filter;
import com.destrostudios.icetea.core.Shader;

public class RadialBlurFilter extends Filter {

    public RadialBlurFilter() {
        sampleDist = 1;
        sampleStrength = 2.2f;
    }
    private float sampleDist;
    private float sampleStrength;

    @Override
    public void init(Application application) {
        super.init(application);
        fragmentShader = new Shader("shaders/filters/radialBlur.frag");
    }

    @Override
    protected void updateUniformData() {
        super.updateUniformData();
        uniformData.setFloat("sampleDist", sampleDist);
        uniformData.setFloat("sampleStrength", sampleStrength);
    }
}
