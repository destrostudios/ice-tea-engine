#version 450
#extension GL_ARB_separate_shader_objects : enable

layout(location = 0) in vec2 vertexTexCoord;

layout(location = 0) out vec4 outColor;

void main() {
    outColor = shaderNode_alphaPulsate(shaderNode_texCoordColor(vertexTexCoord), params.time);
}