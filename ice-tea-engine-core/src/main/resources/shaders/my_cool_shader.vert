#version 450
#extension GL_ARB_separate_shader_objects : enable

layout(binding = 0) uniform Camera {
    mat4 proj;
    mat4 view;
} camera;

layout(binding = 1) uniform Geometry {
    mat4 model;
} geometry;

layout(location = 0) in vec3 modelSpaceVertexPosition;
layout(location = 2) in vec2 modelSpaceVertexTexCoord;

layout(location = 0) out vec2 vertexTexCoord;

void main() {
    gl_Position = camera.proj * camera.view * geometry.model * vec4(modelSpaceVertexPosition, 1);

    vertexTexCoord = modelSpaceVertexTexCoord;
}