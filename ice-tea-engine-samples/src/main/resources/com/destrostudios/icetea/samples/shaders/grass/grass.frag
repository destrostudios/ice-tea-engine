#version 450

// @import core/light/frag
// @import core/shadow

layout(location = 0) in vec4 inWorldPosition;
layout(location = 1) in vec4 inViewPosition;
layout(location = 2) in vec3 inViewNormal;
layout(location = 3) in vec3 inViewLightDirection;
layout(location = 4) in vec2 inVertexTexCoord;

layout(location = 0) out vec4 outColor;

void main() {
    outColor = mix(params.baseColor, params.tipColor, inVertexTexCoord.y) * texture(bladeMap, vec2(inVertexTexCoord.x, 1 - inVertexTexCoord.y));

    #ifdef LIGHT
        LightInfo lightInfo = shaderLib_light_getLightInfo();

        float shadowFactor = 1;
        #ifdef SHADOWINFO
            uint shadowCascadeIndex = 0;
            for (uint i = 0; i < (shadowInfo.splitDepths.length() - 1); i++) {
                if (inViewPosition.z < shadowInfo.splitDepths[i]) {
                    shadowCascadeIndex = i + 1;
                }
            }
            ShadowResult shadowResult = shaderLib_shadow_getShadowResult(inWorldPosition, inViewPosition, shadowCascadeIndex, shadowInfo.viewProjectionMatrices[shadowCascadeIndex], shadowInfo.brightness, shadowInfo.cascadeDebugColors, shadowMap);
            shadowFactor = shadowResult.shadowFactor;
            outColor *= shadowResult.debugColor;
        #endif

        float shininess = 32;
        outColor.rgb = outColor.rgb * shaderLib_light_getPhongLightColor(
            lightInfo,
            inViewPosition.xyz,
            inViewNormal,
            inViewLightDirection,
            shininess,
            shadowFactor
        );
    #endif
}
