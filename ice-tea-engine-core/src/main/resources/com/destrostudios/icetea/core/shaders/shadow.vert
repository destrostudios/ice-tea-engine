#version 450

layout (push_constant) uniform pushConstants {
    int cascadeIndex;
} constants;

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
    gl_Position = shadowInfo.viewProjectionMatrices[constants.cascadeIndex] * worldPosition;

    #ifdef CAMERA_CLIPPLANE
        if (camera.clipPlane.length() > 0) {
            gl_ClipDistance[0] = dot(worldPosition, camera.clipPlane);
        }
    #endif
}
