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
	if (camera.clipPlane.length() > 0) {
		gl_ClipDistance[0] = dot(worldPosition0, camera.clipPlane);
	}
	outBiomeColor = inBiomeColor[0];
	outShadowMapPosition = shaderNode_shadow_getShadowMapPosition(shadowMapLight.proj, shadowMapLight.view, worldPosition0);
	outLightVertexInfo = shaderNode_light_getVertexInfo_DirectionalLight(camera.view, geometry.model, worldPosition0, inNormal[0], light.direction);

    EmitVertex();

	gl_Position = camera.proj * camera.view * worldPosition1;
	if (camera.clipPlane.length() > 0) {
		gl_ClipDistance[0] = dot(worldPosition1, camera.clipPlane);
	}
	outBiomeColor = inBiomeColor[1];
	outShadowMapPosition = shaderNode_shadow_getShadowMapPosition(shadowMapLight.proj, shadowMapLight.view, worldPosition1);
	outLightVertexInfo = shaderNode_light_getVertexInfo_DirectionalLight(camera.view, geometry.model, worldPosition1, inNormal[1], light.direction);

    EmitVertex();

	gl_Position = camera.proj * camera.view * worldPosition2;
	if (camera.clipPlane.length() > 0) {
		gl_ClipDistance[0] = dot(worldPosition2, camera.clipPlane);
	}
	outBiomeColor = inBiomeColor[2];
	outShadowMapPosition = shaderNode_shadow_getShadowMapPosition(shadowMapLight.proj, shadowMapLight.view, worldPosition2);
	outLightVertexInfo = shaderNode_light_getVertexInfo_DirectionalLight(camera.view, geometry.model, worldPosition2, inNormal[2], light.direction);

    EmitVertex();

    EndPrimitive();
}


