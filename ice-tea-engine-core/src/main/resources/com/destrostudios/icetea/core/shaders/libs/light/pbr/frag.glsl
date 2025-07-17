const float PI = 3.14159265359;

vec3 shaderLib_light_getWorldNormalFromNormalMap(sampler2D normalMap, vec2 texCoords, vec3 worldPosition, vec3 worldNormal, vec3 worldTangent, float tangentHandedness) {
    vec3 tangentNormal = (texture(normalMap, texCoords).rgb * 2) - 1;

    vec3 N = worldNormal;
    vec3 T;
    vec3 B;

    if (worldTangent != vec3(0)) {
        T = worldTangent;
        B = normalize(cross(N, T) * tangentHandedness);
    } else {
        // If no tangent is provided, we fallback to computing an approximates tangent+bitangent from derivatives
        vec3 Q1 = dFdx(worldPosition);
        vec3 Q2 = dFdy(worldPosition);
        vec2 st1 = dFdx(texCoords);
        vec2 st2 = dFdy(texCoords);
        T = normalize((Q1 * st2.y) - (Q2 * st1.y));
        B = -normalize(cross(N, T));
    }

    mat3 TBN = mat3(T, B, N);
    return normalize(TBN * tangentNormal);
}

float shaderLib_light_getDistributionGGX(vec3 N, vec3 H, float roughness) {
    float a = roughness * roughness;
    float a2 = a * a;
    float NdotH = max(dot(N, H), 0);
    float NdotH2 = NdotH * NdotH;

    float nom = a2;
    float denom = (NdotH2 * (a2 - 1) + 1);
    denom = PI * denom * denom;

    return nom / denom;
}

float shaderLib_light_getGeometrySchlickGGX(float NdotV, float roughness) {
    float r = (roughness + 1);
    float k = (r * r) / 8;

    float nom = NdotV;
    float denom = (NdotV * (1 - k)) + k;

    return nom / denom;
}

float shaderLib_light_getGeometrySmith(vec3 N, vec3 V, vec3 L, float roughness) {
    float NdotV = max(dot(N, V), 0);
    float NdotL = max(dot(N, L), 0);
    float ggx2 = shaderLib_light_getGeometrySchlickGGX(NdotV, roughness);
    float ggx1 = shaderLib_light_getGeometrySchlickGGX(NdotL, roughness);
    return ggx1 * ggx2;
}

vec3 shaderLib_light_getFresnelSchlick(float cosTheta, vec3 F0) {
    return F0 + ((1 - F0) * pow(clamp(1 - cosTheta, 0, 1), 5));
}

vec3 shaderLib_light_getFresnelSchlickRoughness(float cosTheta, vec3 F0, float roughness) {
    return F0 + ((max(vec3(1 - roughness), F0) - F0) * pow(clamp(1 - cosTheta, 0, 1), 5));
}

vec3 shaderLib_light_getSingleLightRadiance(LightInfo lightInfo, vec3 worldPosition, vec3 N, vec3 V, vec3 albedo, float roughness, float metallic, vec3 F0) {
    vec3 L;
    float attenuation;
    if (lightInfo.type == LIGHT_TYPE_POINT) {
        vec3 toLight = lightInfo.position - worldPosition;
        float distance = length(toLight);
        L = toLight / distance;
        attenuation = 1 / (distance * distance);
    } else {
        L = normalize(-lightInfo.direction);
        attenuation = 1;
    }

    vec3 H = normalize(V + L);
    vec3 radiance = lightInfo.lightColor * attenuation;

    // Cook-Torrance BRDF
    float NDF = shaderLib_light_getDistributionGGX(N, H, roughness);
    float G = shaderLib_light_getGeometrySmith(N, V, L, roughness);
    vec3 F = shaderLib_light_getFresnelSchlick(max(dot(H, V), 0), F0);

    vec3 numerator = NDF * G * F;
    float denominator = (4 * max(dot(N, V), 0) * max(dot(N, L), 0)) + 0.0001; // + 0.0001 to prevent division by zero
    vec3 specular = numerator / denominator;

    // kS is equal to Fresnel
    vec3 kS = F;
    // For energy conservation, the diffuse and specular light can't be above 1 (unless the surface emits light)
    // To preserve this relationship, the diffuse component (kD) should equal 1 - kS
    vec3 kD = vec3(1) - kS;
    // Multiply kD by the inverse metalness such that only non-metals have diffuse lighting, or a linear blend if partly metal (pure metals have no diffuse light)
    kD *= 1 - metallic;

    // Scale light by NdotL
    float NdotL = max(dot(N, L), 0);

    // Note that we already multiplied the BRDF by the Fresnel (kS) so we won't multiply by kS again
    return ((kD * (albedo / PI)) + specular) * radiance * NdotL;
}

vec3 shaderLib_light_getPbrColor(
    LightInfo lightInfo,
    vec3 worldPosition,
    vec3 cameraPosition,
    vec3 albedo,
    vec3 normal,
    float metallic,
    float roughness,
    float occlusion,
    samplerCube irradianceMap,
    samplerCube prefilteredEnvironmentMap,
    int prefilteredEnvironmentMapMipLevels,
    sampler2D brdfLUT,
    float shadowFactor,
    bool applyToneMapping
) {
    vec3 N = normal;
    vec3 V = normalize(cameraPosition - worldPosition);
    vec3 R = reflect(-V, N);

    // If dia-electric (like plastic), use F0 of 0.04
    // If fully metallic, use the albedo color as F0
    vec3 F0 = mix(vec3(0.04), albedo, metallic);

    // Reflectance equation
    vec3 Lo = vec3(0);
    // TODO: Support multiple lights here by looping and adding their radiances to the outgoing radiance Lo
    Lo += shadowFactor * shaderLib_light_getSingleLightRadiance(lightInfo, worldPosition, N, V, albedo, roughness, metallic, F0);

    vec3 F = shaderLib_light_getFresnelSchlickRoughness(max(dot(N, V), 0), F0, roughness);

    vec3 kS = F;
    vec3 kD = 1 - kS;
    kD *= 1 - metallic;

    vec3 irradiance = texture(irradianceMap, N).rgb;
    vec3 diffuse = irradiance * albedo;

    // Sample both the pre-filter map and the BRDF lut and combine them together as per the split-sum approximation to get the IBL specular part
    vec3 prefilteredColor = textureLod(prefilteredEnvironmentMap, R, roughness * (prefilteredEnvironmentMapMipLevels - 1)).rgb;
    vec2 brdf = texture(brdfLUT, vec2(max(dot(N, V), 0), roughness)).rg;
    vec3 specular = prefilteredColor * ((F * brdf.x) + brdf.y);

    vec3 ambient = ((kD * diffuse) + specular) * occlusion;

    vec3 color = ambient + Lo;

    // FIXME: This shouldn't be a choice (even for normal light intensities), but enabling it currently causes the colors to look really washed/dark. It's also done for rendering the HDR environment map, where the results look as expected. Is something else in the setup wrong?
    if (applyToneMapping) {
        color = color / (color + vec3(1));
    }

    return color;
}
