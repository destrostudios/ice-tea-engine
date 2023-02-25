#version 450

layout(location = 0) in vec4 worldPosition;
layout(location = 1) in vec4 viewPosition;
layout(location = 2) in vec4 inBiomeColor;
layout(location = 3) in LightVertexInfo lightVertexInfo;

layout(location = 0) out vec4 outColor;

void main() {
    float shininess = 32;
    uint shadowCascadeIndex = 0;
    for (uint i = 0; i < (shadowInfo.splitDepths.length() - 1); i++) {
        if (viewPosition.z < shadowInfo.splitDepths[i]) {
            shadowCascadeIndex = i + 1;
        }
    }
    ShadowResult shadowResult = shaderNode_shadow_getShadowResult(worldPosition, viewPosition, shadowCascadeIndex, shadowInfo.viewProjectionMatrices[shadowCascadeIndex], shadowInfo.brightness, shadowInfo.cascadeDebugColors, shadowMapTexture);

    vec4 effectiveLightColor = shaderNode_light_getLightColor(lightVertexInfo, light.lightColor, light.ambientColor, light.specularColor, shininess, shadowResult.shadowFactor);
    outColor = inBiomeColor * effectiveLightColor * shadowResult.debugColor;
}
