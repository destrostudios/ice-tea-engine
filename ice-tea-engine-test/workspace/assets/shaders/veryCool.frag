#version 450

// @import shaders/libs/texCoordColor.glsl
// @import shaders/libs/alphaPulsate.glsl

layout(location = 0) in vec2 vertexTexCoord;

layout(location = 0) out vec4 outColor;

void main() {
    outColor = shaderLib_alphaPulsate(shaderLib_texCoordColor(vertexTexCoord), params.time);
}