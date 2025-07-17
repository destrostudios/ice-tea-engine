#version 450

layout(location = 0) in vec3 inVertexPosition;

layout(location = 0) out vec4 outColor;

void main() {
    vec3 envColor = texture(backgroundMap, inVertexPosition).rgb;

    // HDR tone mapping
    // TODO: Does this assume it's a HDR image? Should this be configurable?
    envColor = envColor / (envColor + vec3(1));

    outColor = vec4(envColor, 1);
}
