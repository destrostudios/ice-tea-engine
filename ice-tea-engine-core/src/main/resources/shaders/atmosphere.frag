#version 450

layout(location = 0) in vec3 inWorldPosition;

layout(location = 0) out vec4 outColor;

const vec3 baseColor = vec3(0.18, 0.27, 0.47);

void main() {
	float red = -0.022 * (abs(inWorldPosition.z) - 20) + 0.18;
	float green = -0.025 * (abs(inWorldPosition.z) - 20) + 0.27;
	float blue = -0.019 * (abs(inWorldPosition.z) - 20) + 0.47;

	outColor = vec4(red, green, blue, 1);
}