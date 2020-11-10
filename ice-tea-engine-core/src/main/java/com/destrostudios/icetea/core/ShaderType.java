package com.destrostudios.icetea.core;

import static org.lwjgl.util.shaderc.Shaderc.*;

public enum ShaderType {

    VERTEX_SHADER(shaderc_glsl_vertex_shader),
    GEOMETRY_SHADER(shaderc_glsl_geometry_shader),
    FRAGMENT_SHADER(shaderc_glsl_fragment_shader);

    ShaderType(int kind) {
        this.kind = kind;
    }
    private final int kind;

    public int getKind() {
        return kind;
    }
}
