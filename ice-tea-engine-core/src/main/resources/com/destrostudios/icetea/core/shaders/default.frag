#version 450

layout(location = 0) in vec4 worldPosition;
layout(location = 1) in vec4 viewPosition;
layout(location = 2) in vec2 vertexTexCoord;
layout(location = 3) in LightVertexInfo lightVertexInfo;

layout(location = 0) out vec4 outColor;

void main() {
    #ifdef DIFFUSEMAP
        outColor = texture(diffuseMap, vertexTexCoord);
    #else
        outColor = vec4(1);
    #endif

    #ifdef PARAMS_COLOR
        outColor *= params.color;
    #endif

    #ifdef LIGHT
        float shininess;
        #ifdef PARAMS_SHININESS
            shininess = params.shininess;
        #else
            shininess = 32;
        #endif

        float shadowFactor;
        #ifdef SHADOWINFO
            uint shadowCascadeIndex = 0;
            for (uint i = 0; i < (shadowInfo.splitDepths.length() - 1); i++) {
                if (viewPosition.z < shadowInfo.splitDepths[i]) {
                    shadowCascadeIndex = i + 1;
                }
            }
            ShadowResult shadowResult = shaderNode_shadow_getShadowResult(worldPosition, viewPosition, shadowCascadeIndex, shadowInfo.viewProjectionMatrices[shadowCascadeIndex], shadowInfo.brightness, shadowInfo.cascadeDebugColors, shadowMapTexture);
            shadowFactor = shadowResult.shadowFactor;
            outColor *= shadowResult.debugColor;
        #else
            shadowFactor = 1;
        #endif

        vec4 effectiveLightColor = shaderNode_light_getLightColor(lightVertexInfo, light.lightColor, light.ambientColor, light.specularColor, shininess, shadowFactor);
        outColor *= effectiveLightColor;
    #endif
}