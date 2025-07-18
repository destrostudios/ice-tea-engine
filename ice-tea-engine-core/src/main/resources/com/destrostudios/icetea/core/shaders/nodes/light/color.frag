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

    #ifdef PBRINFO
        vec3 normal = inWorldNormal;
        #ifdef NORMALMAP
            normal = shaderLib_light_getWorldNormalFromNormalMap(normalMap, inTexCoord, inWorldPosition.xyz, inWorldNormal, inWorldTangent, inTangentHandedness);
        #endif
        #ifdef PARAMS_NORMAL_SCALE
            normal *= params.normalScale;
        #endif

        float metallic = 0;
        float roughness = 1;
        #ifdef METALLICROUGHNESSMAP
            vec2 metallicAndRoughness = texture(metallicRoughnessMap, inTexCoord).bg;
            metallic = metallicAndRoughness.x;
            roughness = metallicAndRoughness.y;
            #ifdef PARAMS_METALLIC
                metallic *= params.metallic;
            #endif
            #ifdef PARAMS_ROUGHNESS
                roughness *= params.roughness;
            #endif
        #else
            #ifdef PARAMS_METALLIC
                metallic = params.metallic;
            #endif
            #ifdef PARAMS_ROUGHNESS
                roughness = params.roughness;
            #endif
        #endif

        float occlusion = 1;
        #ifdef OCCLUSIONMAP
            occlusion = texture(occlusionMap, inTexCoord).r;
        #elif PARAMS_OCCLUSIONSTRENGTRH
            occlusion *= params.occlusionStrength;
        #endif

        outColor.rgb = shaderLib_light_getPbrColor(
            lightInfo,
            inWorldPosition.xyz,
            camera.location,
            outColor.rgb,
            normal,
            metallic,
            roughness,
            occlusion,
            pbrIrradianceMap,
            pbrPrefilteredEnvironmentMap,
            pbrInfo.prefilteredEnvironmentMapMipLevels,
            pbrBrdfLUT,
            shadowFactor,
            pbrInfo.applyToneMapping == 1
        );
    #else
        float shininess = 32;
        #ifdef PARAMS_SHININESS
            shininess = params.shininess;
        #endif

        outColor.rgb = outColor.rgb * shaderLib_light_getPhongLightColor(
            lightInfo,
            inViewPosition.xyz,
            inViewNormal,
            inViewLightDirection,
            shininess,
            shadowFactor
        );
    #endif
#endif
