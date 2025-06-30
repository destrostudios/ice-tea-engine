struct ShadowResult {
    float shadowFactor;
    vec4 debugColor;
};

mat4 shadowBiasMatrix = mat4(
    0.5, 0.0, 0.0, 0.0,
    0.0, 0.5, 0.0, 0.0,
    0.0, 0.0, 1.0, 0.0,
    0.5, 0.5, 0.0, 1.0
);

vec4 shaderLib_shadow_getShadowMapPosition(mat4 projectionViewMatrix, vec4 worldSpaceVertexPosition) {
    return (shadowBiasMatrix * projectionViewMatrix * worldSpaceVertexPosition);
}

float shaderLib_shadow_getShadowFactor(vec4 shadowMapPosition, sampler2DArray shadowMap, uint shadowCascadeIndex, float shadowBrightness) {
    vec4 projectedShadowMapPosition = shadowMapPosition / shadowMapPosition.w;
    if ((projectedShadowMapPosition.z > -1) && (projectedShadowMapPosition.z < 1)) {
        float dist = texture(shadowMap, vec3(projectedShadowMapPosition.xy, shadowCascadeIndex)).r;
        if ((projectedShadowMapPosition.w > 0) && ((projectedShadowMapPosition.z - dist) > 0.01)) {
            return shadowBrightness;
        }
    }
    return 1;
}

vec4 shaderLib_shadow_getCascadeDebugColor(uint shadowCascadeIndex, int cascadeDebugColors) {
    if (cascadeDebugColors == 1) {
        switch(shadowCascadeIndex % 3) {
            case 0: return vec4(1, 0.25, 0.25, 1);
            case 1: return vec4(0.25, 1, 0.25, 1);
            case 2: return vec4(0.25, 0.25, 1, 1);
        }
    }
    return vec4(1);
}

ShadowResult shaderLib_shadow_getShadowResult(vec4 worldPosition, vec4 viewPosition, uint shadowCascadeIndex, mat4 projectionViewMatrix, float shadowBrightness, int cascadeDebugColors, sampler2DArray shadowMap) {
    vec4 shadowMapPosition = shaderLib_shadow_getShadowMapPosition(projectionViewMatrix, worldPosition);
    float shadowFactor = shaderLib_shadow_getShadowFactor(shadowMapPosition, shadowMap, shadowCascadeIndex, shadowBrightness);
    vec4 debugColor = shaderLib_shadow_getCascadeDebugColor(shadowCascadeIndex, cascadeDebugColors);
    return ShadowResult(shadowFactor, debugColor);
}
