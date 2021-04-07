#version 450

layout(location = 0) out vec2 outVertexTexCoord;

void main() {
    vec4 worldPosition = geometry.model * vec4(vertexPosition, 1);
    gl_Position = camera.proj * camera.view * worldPosition;

    #ifdef CAMERA_CLIPPLANE
        if (camera.clipPlane.length() > 0) {
            gl_ClipDistance[0] = dot(worldPosition, camera.clipPlane);
        }
    #endif

    outVertexTexCoord = vertexTexCoord;
}