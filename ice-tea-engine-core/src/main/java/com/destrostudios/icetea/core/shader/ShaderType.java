package com.destrostudios.icetea.core.shader;

import lombok.AllArgsConstructor;
import lombok.Getter;

import static org.lwjgl.util.shaderc.Shaderc.*;

@AllArgsConstructor
@Getter
public enum ShaderType {

    COMPUTE_SHADER(shaderc_glsl_compute_shader),
    FRAGMENT_SHADER(shaderc_glsl_fragment_shader),
    GEOMETRY_SHADER(shaderc_glsl_geometry_shader),
    TESSELLATION_EVALUATION_SHADER(shaderc_glsl_tess_evaluation_shader),
    TESSELLATION_CONTROL_SHADER(shaderc_glsl_tess_control_shader),
    VERTEX_SHADER(shaderc_glsl_vertex_shader);

    private int kind;
}
