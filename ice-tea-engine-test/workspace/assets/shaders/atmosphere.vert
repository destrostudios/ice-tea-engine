#version 450

layout(location = 0) out vec3 outWorldPosition;

void main() {
	vec4 worldPosition = geometry.model * vec4(vertexPosition, 1);
	gl_Position = camera.proj * camera.view * worldPosition;

	outWorldPosition = worldPosition.xyz;
}