#version 450

const vec2 invAtan = vec2(0.1591, 0.3183);
vec2 SampleSphericalMap(vec3 v) {
    vec2 uv = vec2(atan(v.z, v.x), asin(v.y));
    uv *= invAtan;
    uv += 0.5;
    return uv;
}

layout(location = 0) in vec3 inVertexPosition;

layout(location = 0) out vec4 outColor;

void main() {
    vec2 uv = SampleSphericalMap(normalize(inVertexPosition)); // Make sure to normalize inVertexPosition
    vec3 color = texture(equirectangularMap, uv).rgb;

    outColor = vec4(color, 1);
}
