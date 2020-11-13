#version 450
#extension GL_ARB_separate_shader_objects : enable

layout(binding = 2) uniform MaterialParams {
    vec4 color;
} params;

layout(binding = 3) uniform sampler2D texSampler;

layout(location = 0) in vec3 fragColor;
layout(location = 1) in vec2 fragTexCoord;

layout(location = 0) out vec4 outColor;

void main() {
    outColor = mix(texture(texSampler, fragTexCoord), params.color, 0.33);
}