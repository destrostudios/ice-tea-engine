#version 450

// @import core/light
// @import core/shadow

#define PI 3.14159265359f

layout(triangles) in;

layout(triangle_strip, max_vertices = 9) out;

layout(location = 0) out vec4 outWorldPosition;
layout(location = 1) out vec4 outViewPosition;
layout(location = 2) out vec2 outVertexTexCoord;
layout(location = 3) out LightVertexInfo outLightVertexInfo;

float rand(vec3 seed) {
	return fract(sin(dot(seed, vec3(12.9898, 78.233, 53.539))) * 43758.5453);
}

mat3 angleAxis3x3(float angle, vec3 axis) {
	float s = sin(angle);
	float c = cos(angle);

	float t = 1 - c;
	float x = axis.x;
	float y = axis.y;
	float z = axis.z;

	return mat3(
		t * x * x + c, t * x * y - s * z, t * x * z + s * y,
		t * x * y + s * z, t * y * y + c, t * y * z - s * x,
		t * x * z - s * y, t * y * z + s * x, t * z * z + c
	);
}

void emitVertex(vec3 position, vec3 offset, mat3 transformationMatrix, vec2 texCoord) {
	vec4 worldPosition = vec4(position + (transformationMatrix * offset), 1);
	vec4 viewPosition = camera.view * worldPosition;
	gl_Position = camera.proj * viewPosition;
	#ifdef CAMERA_CLIPPLANE
		if (camera.clipPlane.length() > 0) {
			gl_ClipDistance[0] = dot(worldPosition, camera.clipPlane);
		}
	#endif
	outWorldPosition = worldPosition;
	outViewPosition = viewPosition;
	outVertexTexCoord = texCoord;
	#ifdef LIGHT_DIRECTION
		outLightVertexInfo = shaderLib_light_getVertexInfo_DirectionalLight(camera.view, geometry.model, worldPosition, vec3(0, 1, 0), light.direction);
	#endif
	EmitVertex();
}

void emitBlade(vec3 worldPosition) {
	vec2 windTexCoord = fract((worldPosition.xz + (params.time * params.windVelocity) / params.windFrequency));
	vec3 windSample = ((texture(windMap, windTexCoord).rgb * 2) - 1);
	float windAngle = params.windMaxAngle * windSample.z;
	vec3 windAxis = normalize(vec3(windSample.xy, 0));
	mat3 windMatrix = angleAxis3x3(windAngle, windAxis);

	mat3 randRotMatrix = angleAxis3x3(rand(worldPosition.xyz) * 2 * PI, vec3(0, 1, 0));
	mat3 tipTransformationMatrix = windMatrix * randRotMatrix;

	for (int i = 0; i < params.bladeSegments; i++) {
		float progress = (i / float(params.bladeSegments));
		vec3 offset = vec3((1 - progress) * 0.5 * params.bladeWidth, progress * params.bladeHeight, pow(progress, params.bladeBend) * params.bladeForward);
		mat3 transformationMatrix = (i == 0) ? randRotMatrix : tipTransformationMatrix;
		emitVertex(worldPosition, vec3(offset.x, offset.y, offset.z), transformationMatrix, vec2(0, progress));
		emitVertex(worldPosition, vec3(-1 * offset.x, offset.y, offset.z), transformationMatrix, vec2(1, progress));
	}
	emitVertex(worldPosition, vec3(0, params.bladeHeight, params.bladeForward), tipTransformationMatrix, vec2(0.5, 1));
	EndPrimitive();
}

void main() {
	emitBlade(gl_in[0].gl_Position.xyz);
	emitBlade(gl_in[1].gl_Position.xyz);
	emitBlade(gl_in[2].gl_Position.xyz);
}
