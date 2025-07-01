#version 450

// @import core/light
// @import core/shadow

layout(location = 0) in vec4 worldPosition;
layout(location = 1) in vec4 viewPosition;
layout(location = 2) in vec2 vertexTexCoord;
layout(location = 3) in LightVertexInfo lightVertexInfo;

layout(location = 0) out vec4 outColor;

void main() {
    vec4 grassColor = mix(params.baseColor, params.tipColor, vertexTexCoord.y) * texture(bladeMap, vec2(vertexTexCoord.x, 1 - vertexTexCoord.y));

    float shininess = 32;
    uint shadowCascadeIndex = 0;
    for (uint i = 0; i < (shadowInfo.splitDepths.length() - 1); i++) {
        if (viewPosition.z < shadowInfo.splitDepths[i]) {
            shadowCascadeIndex = i + 1;
        }
    }
    ShadowResult shadowResult = shaderLib_shadow_getShadowResult(worldPosition, viewPosition, shadowCascadeIndex, shadowInfo.viewProjectionMatrices[shadowCascadeIndex], shadowInfo.brightness, shadowInfo.cascadeDebugColors, shadowMap);

    vec4 effectiveLightColor = shaderLib_light_getLightColor(lightVertexInfo, light.lightColor, light.ambientColor, light.specularColor, shininess, shadowResult.shadowFactor);
    outColor = grassColor * effectiveLightColor * shadowResult.debugColor;
}
