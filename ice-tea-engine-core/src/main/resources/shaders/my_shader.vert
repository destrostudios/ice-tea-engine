#version 450

layout(location = 0) in vec3 modelSpaceVertexPosition;
layout(location = 2) in vec2 modelSpaceVertexTexCoord;
layout(location = 3) in vec3 modelSpaceVertexNormal;
layout(location = 4) in vec4 jointsIndices;
layout(location = 5) in vec4 jointsWeights;

layout(location = 0) out vec2 vertexTexCoord;
layout(location = 1) out LightVertexInfo lightVertexInfo;
layout(location = 4) out vec4 shadowMapPosition;

void main() {
    vec4 meshPosition = vec4(modelSpaceVertexPosition, 1);

    #ifdef SKELETON_JOINTMATRICES
        mat4 skinMatrix = (jointsWeights.x * skeleton.jointMatrices[int(jointsIndices.x)])
                        + (jointsWeights.y * skeleton.jointMatrices[int(jointsIndices.y)])
                        + (jointsWeights.z * skeleton.jointMatrices[int(jointsIndices.z)])
                        + (jointsWeights.w * skeleton.jointMatrices[int(jointsIndices.w)]);
        meshPosition = skinMatrix * meshPosition;
    #endif

    vec4 worldPosition = geometry.model * meshPosition;
    gl_Position = camera.proj * camera.view * worldPosition;

    #ifdef CAMERA_CLIPPLANE
        if (camera.clipPlane.length() > 0) {
            gl_ClipDistance[0] = dot(worldPosition, camera.clipPlane);
        }
    #endif

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