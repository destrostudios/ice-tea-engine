#version 450
#extension GL_ARB_separate_shader_objects : enable

layout(location = 0) in vec3 modelSpaceVertexPosition;

void main() {
    gl_Position = light.proj * light.view * geometry.model * vec4(modelSpaceVertexPosition, 1);
}
