#version 450

// @import core/light
// @import core/shadow

layout(triangles) in;

layout(triangle_strip, max_vertices = 3) out;

layout(location = 0) in vec3 inNormal[];
layout(location = 1) in vec4 inBiomeColor[];

layout(location = 0) out vec4 outWorldPosition;
layout(location = 1) out vec4 outViewPosition;
layout(location = 2) out vec4 outBiomeColor;
layout(location = 3) out LightVertexInfo outLightVertexInfo;

void main() {
	vec4 worldPosition0 = gl_in[0].gl_Position;
	vec4 worldPosition1 = gl_in[1].gl_Position;
	vec4 worldPosition2 = gl_in[2].gl_Position;

	// TODO: Currently adding the vertices in reverse order so the triangles point upwards - Should be fixed/changed in Grid class?

	vec4 viewPosition2 = camera.view * worldPosition2;
	gl_Position = camera.proj * viewPosition2;
	#ifdef CAMERA_CLIPPLANE
		if (camera.clipPlane.length() > 0) {
			gl_ClipDistance[0] = dot(worldPosition2, camera.clipPlane);
		}
	#endif
	outWorldPosition = worldPosition2;
	outViewPosition = viewPosition2;
	outBiomeColor = inBiomeColor[2];
	#ifdef LIGHT_DIRECTION
		outLightVertexInfo = shaderLib_light_getVertexInfo_DirectionalLight(camera.view, geometry.model, worldPosition2, inNormal[2], light.direction);
	#endif

	EmitVertex();

	vec4 viewPosition1 = camera.view * worldPosition1;
	gl_Position = camera.proj * viewPosition1;
	#ifdef CAMERA_CLIPPLANE
		if (camera.clipPlane.length() > 0) {
			gl_ClipDistance[0] = dot(worldPosition1, camera.clipPlane);
		}
	#endif
	outWorldPosition = worldPosition1;
	outViewPosition = viewPosition1;
	outBiomeColor = inBiomeColor[1];
	#ifdef LIGHT_DIRECTION
		outLightVertexInfo = shaderLib_light_getVertexInfo_DirectionalLight(camera.view, geometry.model, worldPosition1, inNormal[1], light.direction);
	#endif

    EmitVertex();

	vec4 viewPosition0 = camera.view * worldPosition0;
	gl_Position = camera.proj * viewPosition0;
	#ifdef CAMERA_CLIPPLANE
		if (camera.clipPlane.length() > 0) {
			gl_ClipDistance[0] = dot(worldPosition0, camera.clipPlane);
		}
	#endif
	outWorldPosition = worldPosition0;
	outViewPosition = viewPosition0;
	outBiomeColor = inBiomeColor[0];
	#ifdef LIGHT_DIRECTION
		outLightVertexInfo = shaderLib_light_getVertexInfo_DirectionalLight(camera.view, geometry.model, worldPosition0, inNormal[0], light.direction);
	#endif

	EmitVertex();

    EndPrimitive();
}
