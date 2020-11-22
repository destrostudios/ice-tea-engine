#version 450
#extension GL_ARB_separate_shader_objects : enable

layout(location = 0) in vec3 modelSpaceVertexPosition;
layout(location = 2) in vec2 modelSpaceVertexTexCoord;

layout(location = 0) out vec2 vertexTexCoord;

void main() {
    gl_Position = camera.proj * camera.view * geometry.model * vec4(modelSpaceVertexPosition, 1);

    vertexTexCoord = modelSpaceVertexTexCoord;
}