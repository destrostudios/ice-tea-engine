#version 450

#define PI 3.14159265359f

layout(triangles) in;

layout(triangle_strip, max_vertices = 9) out;

layout(location = 0) out vec2 outTexCoord;
layout(location = 1) out vec4 outShadowMapPosition;
layout(location = 2) out LightVertexInfo outLightVertexInfo;

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
	vec4 vertexPosition = vec4(position + (transformationMatrix * offset), 1);
	gl_Position = camera.proj * camera.view * vertexPosition;
	if (camera.clipPlane.length() > 0) {
		gl_ClipDistance[0] = dot(vertexPosition, camera.clipPlane);
	}
	outTexCoord = texCoord;
	outShadowMapPosition = shaderNode_shadow_getShadowMapPosition(shadowMapLight.proj, shadowMapLight.view, vertexPosition);
	outLightVertexInfo = shaderNode_light_getVertexInfo_DirectionalLight(camera.view, geometry.model, vertexPosition, vec3(0, 0, 1), light.direction);
	EmitVertex();
}

void emitBlade(vec3 worldPosition) {
	// TODO: Move to params
	float bladeWidth = 0.02;
	float bladeHeight = 0.3;
	float bladeForward = 0.1;
	float bladeBend = 2;
	float bladeBendDelta = 0.2;
	int bladeSegments = 4;
	vec2 windVelocity = vec2(0.03, 0.015);
	float windFrequency = 1;

	mat3 randRotMatrix = angleAxis3x3(rand(worldPosition.xyz) * 2 * PI, vec3(0, 0, 1));

	vec2 windTexCoord = fract((worldPosition.xy + (params.time * windVelocity) / windFrequency));
	vec3 windSample = ((texture(windMap, windTexCoord).rgb * 2) - 1);
	// TODO: Move to params
	float windAngle = 0.25 * PI * windSample.z;
	vec3 windAxis = normalize(vec3(windSample.xy, 0));
	mat3 windMatrix = angleAxis3x3(windAngle, windAxis);

	mat3 baseTransformationMatrix = randRotMatrix;
	mat3 tipTransformationMatrix = windMatrix * randRotMatrix;

	for (int i = 0; i < bladeSegments; i++) {
		float progress = (i / float(bladeSegments));
		vec3 offset = vec3((1 - progress) * 0.5 * bladeWidth, pow(progress, bladeBend) * bladeForward, progress * bladeHeight);
		mat3 transformationMatrix = (i == 0) ? baseTransformationMatrix : tipTransformationMatrix;
		emitVertex(worldPosition, vec3(offset.x, offset.y, offset.z), transformationMatrix, vec2(0, progress));
		emitVertex(worldPosition, vec3(-1 * offset.x, offset.y, offset.z), transformationMatrix, vec2(1, progress));
	}
	emitVertex(worldPosition, vec3(0, bladeForward, bladeHeight), tipTransformationMatrix, vec2(0.5, 1));
	EndPrimitive();
}

void main() {
	emitBlade(gl_in[0].gl_Position.xyz);
	emitBlade(gl_in[1].gl_Position.xyz);
	emitBlade(gl_in[2].gl_Position.xyz);
}
