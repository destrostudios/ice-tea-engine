#version 450

layout(location = 0) out vec2 vertexTexCoord;

void main() {
    vertexTexCoord = vec2((gl_VertexIndex << 1) & 2, gl_VertexIndex & 2);
    gl_Position = vec4((vertexTexCoord * 2) - 1, 0, 1);
}