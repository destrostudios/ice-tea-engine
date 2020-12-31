package com.destrostudios.icetea.core.shader;

import static org.lwjgl.util.shaderc.Shaderc.*;

public enum ShaderType {

    COMPUTE_SHADER(shaderc_glsl_compute_shader),
    FRAGMENT_SHADER(shaderc_glsl_fragment_shader),
    GEOMETRY_SHADER(shaderc_glsl_geometry_shader),
    TESSELATION_EVALUATION_SHADER(shaderc_glsl_tess_evaluation_shader),
    TESSELATION_CONTROL_SHADER(shaderc_glsl_tess_control_shader),
    VERTEX_SHADER(shaderc_glsl_vertex_shader);

    ShaderType(int kind) {
        this.kind = kind;
    }
    private final int kind;

    public int getKind() {
        return kind;
    }
}
