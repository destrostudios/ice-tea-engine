#version 450

layout(location = 0) in vec2 vertexTexCoord;

layout(location = 0) out vec4 outFragColor;

void main() {
    outFragColor = texture(colorMap, vertexTexCoord);
}