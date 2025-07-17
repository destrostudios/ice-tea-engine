#version 450

// @import core/light/vert
// @import core/shadow

layout(triangles) in;

layout(triangle_strip, max_vertices = 3) out;

layout(location = 0) in vec3 inNormal[];
layout(location = 1) in vec4 inBiomeColor[];

layout(location = 0) out vec4 outWorldPosition;
layout(location = 1) out vec4 outViewPosition;
layout(location = 2) out vec3 outViewNormal;
layout(location = 3) out vec3 outViewLightDirection;
layout(location = 4) out vec4 outBiomeColor;

void main() {
	vec4 worldPosition0 = gl_in[0].gl_Position;
	vec4 worldPosition1 = gl_in[1].gl_Position;
	vec4 worldPosition2 = gl_in[2].gl_Position;
	vec3 modelNormal0 = inNormal[0];
	vec3 modelNormal1 = inNormal[1];
	vec3 modelNormal2 = inNormal[2];

	#ifdef LIGHT
		LightInfo lightInfo = shaderLib_light_getLightInfo();
	#endif

	// TODO: Currently adding the vertices in reverse order so the triangles point upwards - Should be fixed/changed in Grid class?

	vec4 viewPosition2 = camera.view * worldPosition2;
	vec3 viewNormal2 = normalize(mat3(transpose(inverse(camera.view * geometry.model))) * modelNormal2);
	gl_Position = camera.proj * viewPosition2;
	#ifdef CAMERA_CLIPPLANE
		if (camera.clipPlane.length() > 0) {
			gl_ClipDistance[0] = dot(worldPosition2, camera.clipPlane);
		}
	#endif
	outWorldPosition = worldPosition2;
	outViewPosition = viewPosition2;
	outViewNormal = viewNormal2;
	#ifdef LIGHT
		outViewLightDirection = shaderLib_light_getViewLightDirection(lightInfo, camera.view, viewPosition2);
	#endif
	outBiomeColor = inBiomeColor[2];

	EmitVertex();

	vec4 viewPosition1 = camera.view * worldPosition1;
	vec3 viewNormal1 = normalize(mat3(transpose(inverse(camera.view * geometry.model))) * modelNormal1);
	gl_Position = camera.proj * viewPosition1;
	#ifdef CAMERA_CLIPPLANE
		if (camera.clipPlane.length() > 0) {
			gl_ClipDistance[0] = dot(worldPosition1, camera.clipPlane);
		}
	#endif
	outWorldPosition = worldPosition1;
	outViewPosition = viewPosition1;
	outViewNormal = viewNormal1;
	#ifdef LIGHT
		outViewLightDirection = shaderLib_light_getViewLightDirection(lightInfo, camera.view, viewPosition1);
	#endif
	outBiomeColor = inBiomeColor[1];

    EmitVertex();

	vec4 viewPosition0 = camera.view * worldPosition0;
	vec3 viewNormal0 = normalize(mat3(transpose(inverse(camera.view * geometry.model))) * modelNormal0);
	gl_Position = camera.proj * viewPosition0;
	#ifdef CAMERA_CLIPPLANE
		if (camera.clipPlane.length() > 0) {
			gl_ClipDistance[0] = dot(worldPosition0, camera.clipPlane);
		}
	#endif
	outWorldPosition = worldPosition0;
	outViewPosition = viewPosition0;
	outViewNormal = viewNormal0;
	#ifdef LIGHT
		outViewLightDirection = shaderLib_light_getViewLightDirection(lightInfo, camera.view, viewPosition0);
	#endif
	outBiomeColor = inBiomeColor[0];

	EmitVertex();

    EndPrimitive();
}
