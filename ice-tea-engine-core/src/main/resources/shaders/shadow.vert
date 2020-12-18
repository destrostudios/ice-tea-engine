#version 450

layout(location = 0) in vec3 modelSpaceVertexPosition;

void main() {
    gl_Position = light.proj * light.view * geometry.model * vec4(modelSpaceVertexPosition, 1);
}
