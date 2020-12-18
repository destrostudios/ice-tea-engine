#version 450

layout(location = 0) in vec2 vertexTexCoord;

layout(location = 0) out vec4 outFragColor;

void main() {
    vec4 color = texture(colorMap, vertexTexCoord);
    float sepiaMix = dot(vec3(0.3, 0.59, 0.11), color.rgb);
    outFragColor = mix(config.color1, config.color2, sepiaMix);
}