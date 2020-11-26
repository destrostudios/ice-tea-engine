#version 450
#extension GL_ARB_separate_shader_objects : enable

layout(location = 0) in vec2 vertexTexCoord;

layout(location = 0) out vec4 outFragColor;

// Radial blur shader code copied from jMonkeyEngine (jmonkeyengine.org), all rights reserved
void main() {
    float samples[10] = float[](-0.08, -0.05, -0.03, -0.02, -0.01, 0.01, 0.02, 0.03, 0.05, 0.08);

    // 0.5,0.5 is the center of the screen, so substracting texCoord from it will result in a vector pointing to the middle of the screen
    vec2 dir = 0.5 - vertexTexCoord;

    // Calculate the distance to the center of the screen
    float dist = sqrt((dir.x * dir.x) + (dir.y * dir.y));

    // Normalize the direction (reuse the distance)
    dir = dir / dist;

    // This is the original colour of this fragment using only this would result in a nonblurred version
    vec4 colorRes = texture(colorMap, vertexTexCoord);

    vec4 sum = colorRes;

    // Take 10 additional blur samples in the direction towards the center of the screen
    for (int i = 0; i < 10; i++){
        sum += texture(colorMap, vertexTexCoord + (dir * samples[i] * config.sampleDist));
    }

    // We have taken eleven samples
    sum *= 1.0 / 11.0;

    // Weighten the blur effect with the distance to the center of the screen (further out is blurred more)
    float t = dist * config.sampleStrength;
    t = clamp(t, 0, 1);

    // Blend the original color with the averaged pixels
    outFragColor = mix(colorRes, sum, t);
}