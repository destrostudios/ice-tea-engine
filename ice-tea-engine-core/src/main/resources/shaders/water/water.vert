#version 450

layout(location = 0) in vec3 modelSpaceVertexPosition;

layout(location = 0) out vec2 outUV;

void main() {
    gl_Position = geometry.model * vec4(modelSpaceVertexPosition, 1);
    outUV = modelSpaceVertexPosition.xy;
}