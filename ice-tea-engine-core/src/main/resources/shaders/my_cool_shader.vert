#version 450

layout(location = 0) in vec3 modelSpaceVertexPosition;
layout(location = 2) in vec2 modelSpaceVertexTexCoord;

layout(location = 0) out vec2 vertexTexCoord;

void main() {
    vec4 worldPosition = geometry.model * vec4(modelSpaceVertexPosition, 1);
    gl_Position = camera.proj * camera.view * worldPosition;

    if (camera.clipPlane.length() > 0) {
        gl_ClipDistance[0] = dot(worldPosition, camera.clipPlane);
    }

    vertexTexCoord = modelSpaceVertexTexCoord;
}