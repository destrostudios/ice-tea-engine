#version 450

layout(triangles) in;

layout(triangle_strip, max_vertices = 3) out;

layout(location = 0) in vec2 inUV[];

layout(location = 0) out vec3 outPosition;
layout(location = 1) out vec2 outUV;
layout(location = 2) out vec3 outTangent;
layout(location = 3) out vec4 outProjectionPosition;

vec3 tangent;

void calcTangent() {
	vec3 v0 = gl_in[0].gl_Position.xyz;
	vec3 v1 = gl_in[1].gl_Position.xyz;
	vec3 v2 = gl_in[2].gl_Position.xyz;

    vec3 e1 = v1 - v0;
    vec3 e2 = v2 - v0;

    float dU1 = inUV[1].x - inUV[0].x;
    float dV1 = inUV[1].y - inUV[0].y;
    float dU2 = inUV[2].x - inUV[0].x;
    float dV2 = inUV[2].y - inUV[0].y;

    float f = 1 / (dU1 * dV2 - dU2 * dV1);

    vec3 t;

    t.x = f * (dV2 * e1.x - dV1 * e2.x);
    t.y = f * (dV2 * e1.y - dV1 * e2.y);
    t.z = f * (dV2 * e1.z - dV1 * e2.z);

	tangent = normalize(t);
}

void main() {
	float dx, dy, dz;
	vec4 position0 = gl_in[0].gl_Position;
	vec4 position1 = gl_in[1].gl_Position;
	vec4 position2 = gl_in[2].gl_Position;

	float cameraDistance = (distance(gl_in[0].gl_Position.xyz, camera.location)
						  + distance(gl_in[1].gl_Position.xyz, camera.location)
						  + distance(gl_in[2].gl_Position.xyz, camera.location)) / 3;

	if (cameraDistance < (params.displacementRange + params.displacementRangeSurrounding)) {

		if (cameraDistance < params.highDetailRangeGeometry) {
			calcTangent();
		}

		dy = texture(dyMap, inUV[0] + (params.windDirection * params.motion)).r
			* max(0, (-distance(gl_in[0].gl_Position.xyz, camera.location) / params.displacementRange + 1)) * params.displacementScale;
		dx = texture(dxMap, inUV[0] + (params.windDirection * params.motion)).r
			* max(0, (-distance(gl_in[0].gl_Position.xyz, camera.location) / params.displacementRange + 1)) * params.choppiness;
		dz = texture(dzMap, inUV[0] + (params.windDirection * params.motion)).r
			* max(0, (-distance(gl_in[0].gl_Position.xyz, camera.location) / params.displacementRange + 1)) * params.choppiness;

		position0.y += dy;
		position0.x -= dx;
		position0.z -= dz;

		dy = texture(dyMap, inUV[1] + (params.windDirection * params.motion)).r
			* max(0, (-distance(gl_in[1].gl_Position.xyz, camera.location) / params.displacementRange + 1)) * params.displacementScale;
		dx = texture(dxMap, inUV[1] + (params.windDirection * params.motion)).r
			* max(0, (-distance(gl_in[1].gl_Position.xyz, camera.location) / params.displacementRange + 1)) * params.choppiness;
		dz = texture(dzMap, inUV[1] + (params.windDirection * params.motion)).r
			* max(0, (-distance(gl_in[1].gl_Position.xyz, camera.location) / params.displacementRange + 1)) * params.choppiness;

		position1.y += dy;
		position1.x -= dx;
		position1.z -= dz;

		dy = texture(dyMap, inUV[2] + (params.windDirection * params.motion)).r
			* max(0, (-distance(gl_in[2].gl_Position.xyz, camera.location) / params.displacementRange + 1)) * params.displacementScale;
		dx = texture(dxMap, inUV[2] + (params.windDirection * params.motion)).r
			* max(0, (-distance(gl_in[2].gl_Position.xyz, camera.location) / params.displacementRange + 1)) * params.choppiness;
		dz = texture(dzMap, inUV[2] + (params.windDirection * params.motion)).r
			* max(0, (-distance(gl_in[2].gl_Position.xyz, camera.location) / params.displacementRange + 1)) * params.choppiness;

		position2.y += dy;
		position2.x -= dx;
		position2.z -= dz;
	}

	// TODO: Currently adding the vertices in reverse order so the triangles point upwards - Should be fixed/changed in Grid class?

	gl_Position = camera.proj * camera.view * position2;
	if (camera.clipPlane.length() > 0) {
		gl_ClipDistance[0] = dot(position2, camera.clipPlane);
	}
	outUV = inUV[2];
	outPosition = position2.xyz;
	outTangent = tangent;
	outProjectionPosition = gl_Position;
	EmitVertex();

	gl_Position = camera.proj * camera.view * position1;
	if (camera.clipPlane.length() > 0) {
		gl_ClipDistance[0] = dot(position1, camera.clipPlane);
	}
	outUV = inUV[1];
	outPosition = position1.xyz;
	outTangent = tangent;
	outProjectionPosition = gl_Position;
	EmitVertex();

    gl_Position = camera.proj * camera.view * position0;
	if (camera.clipPlane.length() > 0) {
		gl_ClipDistance[0] = dot(position0, camera.clipPlane);
	}
	outUV = inUV[0];
	outPosition = position0.xyz;
	outTangent = tangent;
	outProjectionPosition = gl_Position;
    EmitVertex();

    EndPrimitive();
}
