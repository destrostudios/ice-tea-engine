package com.destrostudios.icetea.core.shader;

import lombok.AllArgsConstructor;
import lombok.Getter;

import static org.lwjgl.util.shaderc.Shaderc.*;

@AllArgsConstructor
@Getter
public enum ShaderType {

    COMPUTE_SHADER(shaderc_glsl_compute_shader, "comp"),
    FRAGMENT_SHADER(shaderc_glsl_fragment_shader, "frag"),
    GEOMETRY_SHADER(shaderc_glsl_geometry_shader, "geom"),
    TESSELLATION_EVALUATION_SHADER(shaderc_glsl_tess_evaluation_shader, "tese"),
    TESSELLATION_CONTROL_SHADER(shaderc_glsl_tess_control_shader, "tesc"),
    VERTEX_SHADER(shaderc_glsl_vertex_shader, "vert");

    private int kind;
    private String extension;
}
