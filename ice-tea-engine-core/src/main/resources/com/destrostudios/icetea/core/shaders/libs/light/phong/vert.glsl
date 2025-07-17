vec3 shaderLib_light_getViewLightDirection(LightInfo lightInfo, mat4 viewMatrix, vec4 viewPosition) {
    if (lightInfo.type == LIGHT_TYPE_POINT) {
        vec3 worldLightPosition = lightInfo.position;
        vec3 viewLightPosition = vec3(viewMatrix * vec4(worldLightPosition, 1));
        return normalize(viewPosition.xyz - viewLightPosition);
    } else {
        vec3 worldLightDirection = lightInfo.direction;
        return vec3(viewMatrix * vec4(worldLightDirection, 0));
    }
}
