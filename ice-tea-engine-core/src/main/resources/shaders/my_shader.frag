#version 450
#extension GL_ARB_separate_shader_objects : enable

layout(binding = 2) uniform MaterialParams {
    vec4 color;
} params;

layout(binding = 3) uniform sampler2D diffuseMap;

layout(location = 0) in vec2 vertexTexCoord;
layout(location = 1) in PhongLightVertexInfo phongLightVertexInfo;

layout(location = 0) out vec4 outColor;

void main() {
    vec4 lightColor = vec4(1.0, 1.0, 1.0, 1.0);
    vec4 ambientColor = vec4(0.1, 0.1, 0.1, 0.1);
    vec4 specularColor = vec4(1.0, 1.0, 1.0, 1.0);
    float shininess = 32;

    vec4 effectiveLightColor = shaderNode_phongLight_getLightColor(phongLightVertexInfo, lightColor, ambientColor, specularColor, shininess);
    vec4 diffuseColor = mix(texture(diffuseMap, vertexTexCoord), params.color, 0.33);

    outColor = effectiveLightColor * diffuseColor;
}