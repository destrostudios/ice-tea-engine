#version 450

layout(location = 0) in vec2 vertexTexCoord;

layout(location = 0) out vec4 outColor;

void main() {
    outColor = shaderNode_alphaPulsate(shaderNode_texCoordColor(vertexTexCoord), params.time);
}