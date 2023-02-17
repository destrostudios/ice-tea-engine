#version 450

layout(location = 0) out vec2 outVertexTexCoord;
layout(location = 1) out vec4 outVertexColor;

void main() {
    vec4 modelSpacePosition = vec4(vertexPosition, 1);

    vec4 worldPosition = geometry.model * modelSpacePosition;
    gl_Position = camera.proj * camera.view * worldPosition;

    outVertexTexCoord = vertexTexCoord;
    outVertexColor = vertexColor;
}
