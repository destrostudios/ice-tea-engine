#version 450

layout(location = 0) in vec3 inPosition;
layout(location = 1) in vec2 inUV;
layout(location = 2) in vec3 inTangent;
layout(location = 3) in vec4 inProjectionPosition;

layout(location = 0) out vec4 outColor;

float getApproximatedFresnel(vec3 normal, vec3 vertexToEye) {
    vec3 halfDirection = normalize(normal + vertexToEye);
    float cosine = dot(halfDirection, vertexToEye);
    float fresnel = params.eta + (1 - params.eta) * pow(max(0, 1 - dot(vertexToEye, normal)), params.fresnelFactor);
    return clamp(pow(fresnel, 1), 0, 1);
}

void main() {
    vec3 vertexToEye = normalize(camera.location - inPosition);
    float cameraDistance = length(camera.location - inPosition);

    // normal
    vec3 normal = normalize(texture(normalMap, inUV + (params.windDirection * params.motion)).rgb);
    if (cameraDistance < params.highDetailRangeTexture){
        float attenuation = clamp((-cameraDistance / params.highDetailRangeTexture) + 1, 0, 1);

        vec3 bitangent = normalize(cross(inTangent, normal));
        mat3 TBN = mat3(inTangent, bitangent, normal);
        vec3 bumpNormal = normalize(texture(normalMap, inUV * params.capillarDownsampling).rgb);
        bumpNormal.z *= params.capillarStrength;
        bumpNormal.xy *= attenuation;

        bumpNormal = normalize(bumpNormal);

        normal = normalize(TBN * bumpNormal);
    }
    // For the high detail matrix multiplication above, we need z to point upwards (also in the normal map) - We correct/flip it here afterwards
    normal = normal.xzy;

    vec3 dudvCoord = normalize((2 * texture(dudvMap, inUV * params.dudvDownsampling + params.distortion).rbg) - 1);
    vec2 projectionCoord = (inProjectionPosition.xy / inProjectionPosition.w) / 2 + 0.5;
    vec2 projectionCoordInvertedX = vec2(1 - projectionCoord.x, projectionCoord.y);

    float fresnel = getApproximatedFresnel(normal, vertexToEye);

    // Reflection
    vec2 reflectionCoords = projectionCoordInvertedX.xy + dudvCoord.rb * params.kReflection;
    reflectionCoords = clamp(reflectionCoords, params.kReflection, 1 - params.kReflection);
    vec3 reflection = texture(reflectionMap, reflectionCoords).rgb;
    reflection *= fresnel;

    // Refraction
    vec2 refractionCoords = projectionCoord.xy + dudvCoord.rb * params.kRefraction;
    refractionCoords = clamp(refractionCoords, params.kRefraction, 1 - params.kRefraction);
    vec3 refraction = texture(refractionMap, refractionCoords).rgb;
    refraction *= 1 - fresnel;

    outColor = vec4(mix(params.waterColor.rgb, reflection + refraction, params.waterColor.a), 1);

    // TEST:
    outColor = vec4(normalize(vec3(normal.x, 0, normal.z)), 1);
    // LightVertexInfo lightVertexInfo = shaderNode_light_getVertexInfo_DirectionalLight(camera.view, geometry.model, vec4(inPosition, 1), normal, light.direction);
    // outColor += shaderNode_light_getLightColor(lightVertexInfo, light.lightColor, light.ambientColor, light.specularColor, 32, 1);
}
