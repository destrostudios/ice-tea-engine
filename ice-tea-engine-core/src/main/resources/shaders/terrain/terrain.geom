#version 450

layout(triangles) in;

layout(triangle_strip, max_vertices = 3) out;

layout(location = 0) in vec3 inNormal[];
layout(location = 1) in vec4 inBiomeColor[];

layout(location = 0) out vec4 outBiomeColor;
layout(location = 1) out vec4 outShadowMapPosition;
layout(location = 2) out LightVertexInfo outLightVertexInfo;

void main() {
	vec4 worldPosition0 = gl_in[0].gl_Position;
	vec4 worldPosition1 = gl_in[1].gl_Position;
	vec4 worldPosition2 = gl_in[2].gl_Position;

    gl_Position = camera.proj * camera.view * worldPosition0;
	#ifdef CAMERA_CLIPPLANE
		if (camera.clipPlane.length() > 0) {
			gl_ClipDistance[0] = dot(worldPosition0, camera.clipPlane);
		}
	#endif
	outBiomeColor = inBiomeColor[0];
	#ifdef SHADOWMAPLIGHT
		outShadowMapPosition = shaderNode_shadow_getShadowMapPosition(shadowMapLight.proj, shadowMapLight.view, worldPosition0);
	#endif
	#ifdef LIGHT_DIRECTION
		outLightVertexInfo = shaderNode_light_getVertexInfo_DirectionalLight(camera.view, geometry.model, worldPosition0, inNormal[0], light.direction);
	#endif

    EmitVertex();

	gl_Position = camera.proj * camera.view * worldPosition1;
	#ifdef CAMERA_CLIPPLANE
		if (camera.clipPlane.length() > 0) {
			gl_ClipDistance[0] = dot(worldPosition1, camera.clipPlane);
		}
	#endif
	outBiomeColor = inBiomeColor[1];
	#ifdef SHADOWMAPLIGHT
		outShadowMapPosition = shaderNode_shadow_getShadowMapPosition(shadowMapLight.proj, shadowMapLight.view, worldPosition1);
	#endif
	#ifdef LIGHT_DIRECTION
		outLightVertexInfo = shaderNode_light_getVertexInfo_DirectionalLight(camera.view, geometry.model, worldPosition1, inNormal[1], light.direction);
	#endif

    EmitVertex();

	gl_Position = camera.proj * camera.view * worldPosition2;
	#ifdef CAMERA_CLIPPLANE
		if (camera.clipPlane.length() > 0) {
			gl_ClipDistance[0] = dot(worldPosition2, camera.clipPlane);
		}
	#endif
	outBiomeColor = inBiomeColor[2];
	#ifdef SHADOWMAPLIGHT
		outShadowMapPosition = shaderNode_shadow_getShadowMapPosition(shadowMapLight.proj, shadowMapLight.view, worldPosition2);
	#endif
	#ifdef LIGHT_DIRECTION
		outLightVertexInfo = shaderNode_light_getVertexInfo_DirectionalLight(camera.view, geometry.model, worldPosition2, inNormal[2], light.direction);
	#endif

    EmitVertex();

    EndPrimitive();
}


