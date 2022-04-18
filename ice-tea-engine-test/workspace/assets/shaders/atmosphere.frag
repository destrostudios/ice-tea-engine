#version 450

layout(location = 0) in vec3 inWorldPosition;

layout(location = 0) out vec4 outColor;

const vec3 colorTop = vec3(0, 0, 0.6);
const vec3 colorBottom = vec3(0.34, 0.54, 0.7);

void main() {
	outColor = vec4(mix(colorBottom, colorTop, (inWorldPosition.y / 31)), 1);
}
