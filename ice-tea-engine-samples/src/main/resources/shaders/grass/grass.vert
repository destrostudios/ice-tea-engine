#version 450

void main() {
    gl_Position = geometry.model * vec4(vertexPosition, 1);
}
