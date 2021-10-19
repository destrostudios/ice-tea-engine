#version 450

layout(location = 0) out vec2 outVertexTexCoord;
layout(location = 1) out LightVertexInfo lightVertexInfo;
layout(location = 4) out vec4 shadowMapPosition;

void main() {
    vec4 modelSpacePosition = vec4(vertexPosition, 1);

    #ifdef SKELETON_JOINTMATRICES
        mat4 skinMatrix = (jointsWeights.x * skeleton.jointMatrices[int(jointsIndices.x)])
                        + (jointsWeights.y * skeleton.jointMatrices[int(jointsIndices.y)])
                        + (jointsWeights.z * skeleton.jointMatrices[int(jointsIndices.z)])
                        + (jointsWeights.w * skeleton.jointMatrices[int(jointsIndices.w)]);
        modelSpacePosition = skinMatrix * modelSpacePosition;
    #endif

    vec4 worldPosition = geometry.model * modelSpacePosition;
    gl_Position = camera.proj * camera.view * worldPosition;

    #ifdef CAMERA_CLIPPLANE
        if (camera.clipPlane.length() > 0) {
            gl_ClipDistance[0] = dot(worldPosition, camera.clipPlane);
        }
    #endif

    #ifdef VERTEX_VERTEXTEXCOORD
        outVertexTexCoord = vertexTexCoord;
    #endif

    #ifdef VERTEX_VERTEXNORMAL
        #ifdef LIGHT_DIRECTION
            lightVertexInfo = shaderNode_light_getVertexInfo_DirectionalLight(camera.view, geometry.model, worldPosition, vertexNormal, light.direction);
        #elif LIGHT_TRANSLATION
            lightVertexInfo = shaderNode_light_getVertexInfo_SpotLight(camera.view, geometry.model, worldPosition, vertexNormal, light.translation);
        #endif
    #endif

    #ifdef SHADOWMAPLIGHT
        shadowMapPosition = shaderNode_shadow_getShadowMapPosition(shadowMapLight.proj, shadowMapLight.view, worldPosition);
    #endif
}
