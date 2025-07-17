#version 450

layout(location = 0) out vec3 outVertexPosition;

void main() {
    vec4 modelPosition = vec4(vertexPosition, 1);
    vec4 worldPosition = geometry.model * modelPosition;
    vec4 viewPosition = mat4(mat3(camera.view)) * worldPosition; // Remove translation
    vec4 projectionPosition = camera.proj * viewPosition;
    gl_Position = projectionPosition.xyww;
    outVertexPosition = vertexPosition;
}
