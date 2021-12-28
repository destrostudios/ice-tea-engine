#version 450

layout(location = 0) in vec4 inBiomeColor;
layout(location = 1) in vec4 inShadowMapPosition;
layout(location = 2) in LightVertexInfo lightVertexInfo;

layout(location = 0) out vec4 outColor;

void main() {
    float shininess = 32;
    float shadowFactor = shaderNode_shadow_getShadowFactor(inShadowMapPosition, shadowMapTexture);
    vec4 effectiveLightColor = shaderNode_light_getLightColor(lightVertexInfo, light.lightColor, light.ambientColor, light.specularColor, shininess, shadowFactor);
    outColor = inBiomeColor * effectiveLightColor;
}
