#version 450

layout(push_constant) uniform pushConstants {
    mat4 modelViewProjectionMatrix;
} constants;

layout(location = 0) out vec3 outVertexPosition;

void main() {
    gl_Position = constants.modelViewProjectionMatrix * vec4(vertexPosition, 1);
    outVertexPosition = vertexPosition;
}
