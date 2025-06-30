vec4 shaderNode_whoosh_getWorldPosition(vec3 vertexPosition, vec3 meshBoundsCenter, vec3 meshBoundsExtent, vec3 targetPositionOld, vec3 targetPositionNew, float duration, float startTime, float time) {
    vec3 vertexFarnessXYZ = abs((vertexPosition - meshBoundsCenter) / meshBoundsExtent);
    float vertexFarness = max(max(vertexFarnessXYZ.x, vertexFarnessXYZ.y), vertexFarnessXYZ.z);

    float progress = clamp((time - startTime) / (duration * vertexFarness), 0, 1);
    return vec4(mix(targetPositionOld, targetPositionNew, progress), 0);
}
