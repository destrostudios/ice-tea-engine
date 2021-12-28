package com.destrostudios.icetea.samples.filter;

import com.destrostudios.icetea.core.Application;
import com.destrostudios.icetea.core.filter.Filter;
import com.destrostudios.icetea.core.shader.Shader;
import org.joml.Vector4f;

public class SepiaFilter extends Filter {

    public SepiaFilter() {
        color1 = new Vector4f(0.2f, 0.05f, 0, 1);
        color2 = new Vector4f(1, 0.9f, 0.5f, 1);
    }
    private Vector4f color1;
    private Vector4f color2;

    @Override
    public void init(Application application) {
        super.init(application);
        fragmentShader = new Shader("com/destrostudios/icetea/samples/shaders/filters/sepia.frag");
    }

    @Override
    protected void updateUniformData() {
        super.updateUniformData();
        uniformData.setVector4f("color1", color1);
        uniformData.setVector4f("color2", color2);
    }
}
