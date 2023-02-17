#version 450

layout(location = 0) in vec2 vertexTexCoord;
layout(location = 1) in vec4 vertexColor;

layout(location = 0) out vec4 outColor;

void main() {
    outColor = vertexColor * texture(diffuseMap, vertexTexCoord);
}