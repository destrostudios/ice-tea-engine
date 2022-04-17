package com.destrostudios.icetea.samples.filter;

import com.destrostudios.icetea.core.filter.Filter;
import com.destrostudios.icetea.core.shader.Shader;
import org.joml.Vector4f;

public class SepiaFilter extends Filter {

    public SepiaFilter() {
        this(new Vector4f(0.2f, 0.05f, 0, 1), new Vector4f(1, 0.9f, 0.5f, 1));
    }

    public SepiaFilter(Vector4f color1, Vector4f color2) {
        this.color1 = color1;
        this.color2 = color2;
        fragmentShader = new Shader("com/destrostudios/icetea/samples/shaders/filters/sepia.frag");
    }
    private Vector4f color1;
    private Vector4f color2;

    @Override
    protected void updateUniformData() {
        super.updateUniformData();
        uniformData.setVector4f("color1", color1);
        uniformData.setVector4f("color2", color2);
    }
}
