#version 450
#extension GL_ARB_separate_shader_objects : enable

layout(location = 0) in vec3 modelSpaceVertexPosition;
layout(location = 2) in vec2 modelSpaceVertexTexCoord;
layout(location = 3) in vec3 modelSpaceVertexNormal;

layout(location = 0) out vec2 vertexTexCoord;
layout(location = 1) out LightVertexInfo lightVertexInfo;
layout(location = 4) out vec4 shadowMapPosition;

void main() {
    gl_Position = camera.proj * camera.view * geometry.model * vec4(modelSpaceVertexPosition, 1);

    vertexTexCoord = modelSpaceVertexTexCoord;

    #ifdef LIGHT_DIRECTION
        lightVertexInfo = shaderNode_light_getVertexInfo_DirectionalLight(camera.view, geometry.model, modelSpaceVertexPosition, modelSpaceVertexNormal, light.direction);
    #elif LIGHT_TRANSLATION
        lightVertexInfo = shaderNode_light_getVertexInfo_SpotLight(camera.view, geometry.model, modelSpaceVertexPosition, modelSpaceVertexNormal, light.translation);
    #endif

    #ifdef SHADOWMAPLIGHT
        shadowMapPosition = shaderNode_shadow_getShadowMapPosition(shadowMapLight.proj, shadowMapLight.view, geometry.model, modelSpaceVertexPosition);
    #endif
}