#version 450

layout(vertices = 16) out;

const int CD = 0;
const int AC = 1;
const int AB = 2;
const int BD = 3;

float getLodFactor(float cameraDistance) {
	return max(0, (params.tessellationFactor / pow(cameraDistance, params.tessellationSlope)) - params.tessellationShift);
}

void main() {
	if (gl_InvocationID == 0) {
		// D ---- B        15  11   7   3
		// |      |        14  10   6   2
		// |      |        13   9   5   1
		// C ---- A        12   8   4   0

		vec3 abMid = vec3((gl_in[0].gl_Position.x + gl_in[3].gl_Position.x) / 2,
						  (gl_in[0].gl_Position.y + gl_in[3].gl_Position.y) / 2,
						  (gl_in[0].gl_Position.z + gl_in[3].gl_Position.z) / 2);

		vec3 bdMid = vec3((gl_in[3].gl_Position.x + gl_in[15].gl_Position.x) / 2,
						  (gl_in[3].gl_Position.y + gl_in[15].gl_Position.y) / 2,
						  (gl_in[3].gl_Position.z + gl_in[15].gl_Position.z) / 2);

		vec3 cdMid = vec3((gl_in[15].gl_Position.x + gl_in[12].gl_Position.x) / 2,
						  (gl_in[15].gl_Position.y + gl_in[12].gl_Position.y) / 2,
						  (gl_in[15].gl_Position.z + gl_in[12].gl_Position.z) / 2);

		vec3 acMid = vec3((gl_in[12].gl_Position.x + gl_in[0].gl_Position.x) / 2,
						  (gl_in[12].gl_Position.y + gl_in[0].gl_Position.y) / 2,
						  (gl_in[12].gl_Position.z + gl_in[0].gl_Position.z) / 2);

		vec3 cameraModelSpaceLocation = vec3(inverse(geometry.model) * vec4(camera.location, 1));

		float cameraDistanceAB = distance(abMid, cameraModelSpaceLocation);
		float cameraDistanceBD = distance(bdMid, cameraModelSpaceLocation);
		float cameraDistanceCD = distance(cdMid, cameraModelSpaceLocation);
		float cameraDistanceAC = distance(acMid, cameraModelSpaceLocation);

		gl_TessLevelOuter[AB] = mix(1, gl_MaxTessGenLevel, getLodFactor(cameraDistanceAB));
		gl_TessLevelOuter[BD] = mix(1, gl_MaxTessGenLevel, getLodFactor(cameraDistanceBD));
		gl_TessLevelOuter[CD] = mix(1, gl_MaxTessGenLevel, getLodFactor(cameraDistanceCD));
		gl_TessLevelOuter[AC] = mix(1, gl_MaxTessGenLevel, getLodFactor(cameraDistanceAC));

		gl_TessLevelInner[0] = (max(gl_TessLevelOuter[BD], gl_TessLevelOuter[AC]) / 2);
		gl_TessLevelInner[1] = (max(gl_TessLevelOuter[AB], gl_TessLevelOuter[CD]) / 2);
	}

	gl_out[gl_InvocationID].gl_Position = gl_in[gl_InvocationID].gl_Position;
}
