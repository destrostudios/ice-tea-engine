vec3 shaderLib_light_getPhongLightColor(LightInfo lightInfo, vec3 viewPosition, vec3 viewNormal, vec3 viewLightDirection, float shininess, float shadowFactor) {
    vec3 ambient = lightInfo.phongAmbientColor * lightInfo.lightColor;

    float cosTheta = max(dot(viewNormal, -1 * viewLightDirection), 0);
    vec3 diffuse = cosTheta * lightInfo.lightColor;

    vec3 viewDirection = normalize(-1 * viewPosition); // The viewer is always at (0, 0, 0) in view space
    vec3 reflectDirection = reflect(viewLightDirection, viewNormal);
    float cosAlpha = pow(max(dot(viewDirection, reflectDirection), 0), shininess);
    vec3 specular = lightInfo.phongSpecularColor * cosAlpha * lightInfo.lightColor;

    return ambient + shadowFactor * (diffuse + specular);
}
