#version 450

layout(location = 0) in vec2 vertexTexCoord;
layout(location = 1) in LightVertexInfo lightVertexInfo;
layout(location = 4) in vec4 shadowMapPosition;

layout(location = 0) out vec4 outColor;

void main() {
    #ifdef DIFFUSEMAP
        outColor = texture(diffuseMap, vertexTexCoord);
    #else
        outColor = vec4(1);
    #endif

    #ifdef PARAMS_COLOR
        outColor = mix(outColor, params.color, 0.33);
    #endif

    #ifdef LIGHT
        float shininess;
        #ifdef PARAMS_SHININESS
            shininess = params.shininess;
        #else
            shininess = 32;
        #endif

        float shadowFactor;
        #ifdef SHADOWMAPTEXTURE
            shadowFactor = shaderNode_shadow_getShadowFactor(shadowMapPosition, shadowMapTexture);
        #else
            shadowFactor = 1;
        #endif

        vec4 effectiveLightColor = shaderNode_light_getLightColor(lightVertexInfo, light.lightColor, light.ambientColor, light.specularColor, shininess, shadowFactor);
        outColor = outColor * effectiveLightColor;
    #endif
}