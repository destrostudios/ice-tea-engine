#version 450

layout(location = 0) out vec2 outUV;

void main() {
    gl_Position = geometry.model * vec4(vertexPosition, 1);
    outUV = vertexPosition.xy;
}