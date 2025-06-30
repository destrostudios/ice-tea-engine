#version 450

// @import core/light
// @import core/shadow
// @import samples/noise

layout(quads, fractional_odd_spacing, cw) in;

layout(location = 0) out vec3 outNormal;
layout(location = 1) out vec4 outBiomeColor;

float getElevation(float nx, float nz) {
	float e = (pow(1.00 * shaderLib_noise_noise(1 * 3 * nx, 1 * 3 * nz), 4)
		+ pow(1.00 * shaderLib_noise_noise(2 * 0.5 * nx,  2 * 0.5 * nz), 1.5)
		+ 0.25 * shaderLib_noise_noise(4 * 1 * nx,  4 * 1 * nz)
		+ 0.13 * shaderLib_noise_noise(8 * 1 * nx,  8 * 1 * nz)
		+ 0.06 * shaderLib_noise_noise(16 * 1 * nx, 16 * 1 * nz)
		+ 0.03 * shaderLib_noise_noise(32 * 1 * nx, 32 * 1 * nz));
	e = e / (1.00 + 1.00 + 0.25 + 0.13 + 0.06 + 0.03);
	e = pow(e, 2);
	return e;
}

float getElevation(vec2 p) {
	return getElevation(p.x, p.y);
}

float getMoisture(float nx, float nz) {
	float frequency = 3;
	float m = (1.00 * shaderLib_noise_noise(1 * frequency * nx, 1 * frequency * nz)
		+ 0.75 * shaderLib_noise_noise(2 * frequency * nx, 2 * frequency * nz)
		+ 0.33 * shaderLib_noise_noise(4 * frequency * nx, 4 * frequency * nz)
		+ 0.33 * shaderLib_noise_noise(8 * frequency * nx, 8 * frequency * nz)
		+ 0.33 * shaderLib_noise_noise(16 * frequency * nx, 16 * frequency * nz)
		+ 0.50 * shaderLib_noise_noise(32 * frequency * nx, 32 * frequency * nz));
	m = m / (1.00 + 0.75 + 0.33 + 0.33 + 0.33 + 0.50);
	return m;
}

int getBiome(float e, float m) {
	if (e < 0.09) return 0; // OCEAN
	if (e < 0.12) return 1; // BEACH
	if (e > 0.55) {
		if (m < 0.1) return 2; // SCORCHED
		if (m < 0.2) return 3; // BARE
		if (m < 0.3) return 4; // TUNDRA
		return 13; // SNOW;
	}
	if (e > 0.3) {
		if (m < 0.33) return 5; // TEMPERATE_DESERT
		if (m < 0.66) return 6; // SHRUBLAND
		return 14; // TAIGA
	}
	if (e > 0.2) {
		if (m < 0.16) return 5; // TEMPERATE_DESERT
		if (m < 0.50) return 7; // GRASSLAND
		if (m < 0.83) return 8; // TEMPERATE_DECIDUOUS_FOREST
		return 9; // TEMPERATE_RAIN_FOREST
	}
	if (m < 0.16) return 10; // SUBTROPICAL_DESERT
	if (m < 0.33) return 7; // GRASSLAND
	if (m < 0.66) return 11; // TROPICAL_SEASONAL_FOREST
	return 12; // TROPICAL_RAIN_FOREST
}

vec4 getBiomeColor(int biome) {
	switch (biome) {
		// OCEAN
		case 0: return vec4(0.1, 0.1, 0.9, 1);
		// BEACH
		case 1: return vec4(0.8, 0.7, 0.2, 1);
		// SCORCHED
		case 2: return vec4(0.2, 0, 0.05, 1);
		// BARE
		case 3: return vec4(0.3, 0.1, 0.1, 1);
		// TUNDRA
		case 4: return vec4(0.5, 0.5, 0.2, 1);
		// TEMPERATE_DESERT
		case 5: return vec4(0.6, 0.2, 0.1, 1);
		// SHRUBLAND
		case 6: return vec4(0.3, 0.7, 0.1, 1);
		// GRASSLAND
		case 7: return vec4(0.1, 0.8, 0, 1);
		// TEMPERATE_DECIDUOUS_FOREST
		case 8: return vec4(0.1, 0.6, 0.1, 1);
		// TEMPERATE_RAIN_FOREST
		case 9: return vec4(0.2, 0.7, 0.4, 1);
		// SUBTROPICAL_DESERT
		case 10: return vec4(0.7, 0.5, 0.1, 1);
		// TROPICAL_SEASONAL_FOREST
		case 11: return vec4(0.3, 0.6, 0, 1);
		// TROPICAL_RAIN_FOREST
		case 12: return vec4(0.1, 0.7, 0.2, 1);
		// SNOW
		case 13: return vec4(1, 1, 1, 1);
		// TAIGA
		case 14: return vec4(0.2, 0.4, 0.1, 1);
	}
	return vec4(0, 0, 0, 1);
}

vec3 getNormal(vec2 P) {
	// Get neighbor elevations with an arbitrary small offset
	float offset = 0.00001;
	vec3 off = vec3(offset, offset, 0);
	float elevationLeft = getElevation(P.xy - off.xz);
	float elevationRight = getElevation(P.xy + off.xz);
	float elevationDown = getElevation(P.xy - off.zy);
	float elevationUp = getElevation(P.xy + off.zy);
	// Calculate normal
	vec3 normal = vec3(0);
	normal.x = elevationLeft - elevationRight;
	normal.y = 2 * offset;
	normal.z = elevationDown - elevationUp;
	normal = normalize(normal);
	return normal;
}

void main() {
    float u = gl_TessCoord.x;
    float v = gl_TessCoord.y;

	vec4 modelPosition = (
		(1 - u) * (1 - v) * gl_in[12].gl_Position +
		u * (1 - v) * gl_in[0].gl_Position +
		u * v * gl_in[3].gl_Position +
		(1 - u) * v * gl_in[15].gl_Position
	);

	float elevation = getElevation(modelPosition.x, modelPosition.z);
	float moisture = getMoisture(modelPosition.x, modelPosition.z);
	modelPosition.y = elevation;

	vec4 worldPosition = geometry.model * modelPosition;

	gl_Position = worldPosition;
	outNormal = getNormal(modelPosition.xz);
	outBiomeColor = getBiomeColor(getBiome(elevation, moisture));
}
