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
            if (inViewPosition.z < shadowInfo.splitDepths[i]) {
                shadowCascadeIndex = i + 1;
            }
        }
        ShadowResult shadowResult = shaderLib_shadow_getShadowResult(inWorldPosition, inViewPosition, shadowCascadeIndex, shadowInfo.viewProjectionMatrices[shadowCascadeIndex], shadowInfo.brightness, shadowInfo.cascadeDebugColors, shadowMap);
        shadowFactor = shadowResult.shadowFactor;
        outColor *= shadowResult.debugColor;
    #else
        shadowFactor = 1;
    #endif

    vec4 effectiveLightColor = shaderLib_light_getLightColor(inLightVertexInfo, light.lightColor, light.ambientColor, light.specularColor, shininess, shadowFactor);
    outColor *= effectiveLightColor;
#endif
